package br.com.mechanic.account.service.textai;

import br.com.mechanic.account.config.OpenAiProperties;
import br.com.mechanic.account.constant.AccountUpdateValidationConstants;
import br.com.mechanic.account.constant.TextAiAssistantContentSanitizerConstants;
import br.com.mechanic.account.constant.TextAiAssistantOpenAiUserPayloadJsonConstants;
import br.com.mechanic.account.constant.TextAiAssistantValidationConstants;
import br.com.mechanic.account.constant.TopicServiceLogConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.textai.AccountTextAiSession;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import br.com.mechanic.account.exception.AccountException;
import br.com.mechanic.account.repository.account.jpa.AccountRepositoryJpa;
import br.com.mechanic.account.repository.textai.jpa.AccountTextAiSessionRepositoryJpa;
import br.com.mechanic.account.security.ApiAccessValidation;
import br.com.mechanic.account.service.openai.OpenAiAssistantThreadTurnPort;
import br.com.mechanic.account.service.openai.OpenAiAssistantThreadTurnResult;
import br.com.mechanic.account.service.request.TextAiAssistantRequest;
import br.com.mechanic.account.service.response.TextAiAssistantResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TextAiAssistantService implements TextAiAssistantServiceBO {

    private final ApiAccessValidation apiAccessValidation;
    private final AccountRepositoryJpa accountRepositoryJpa;
    private final AccountTextAiSessionRepositoryJpa sessionRepositoryJpa;
    private final OpenAiAssistantThreadTurnPort openAiAssistantThreadTurnPort;
    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    @Transactional
    public TextAiAssistantResponse process(Long accountId, TextAiAssistantRequest request) {
        apiAccessValidation.requireTextAiAssistantAccess(accountId);
        if (!openAiProperties.usesAssistantTextAi()) {
            throw new AccountException(TextAiAssistantValidationConstants.MESSAGE_ASSISTANT_TEXT_AI_NOT_CONFIGURED);
        }
        Account account = getAccountOrThrowAndAssertActive(accountId);
        if (request.threadId() != null && !request.threadId().isBlank()) {
            return processContinuingTurn(account, request);
        }
        return processFirstTurn(account, request);
    }

    private TextAiAssistantResponse processFirstTurn(Account account, TextAiAssistantRequest request) {
        validateFirstTurnRequest(request);
        LocalDateTime now = LocalDateTime.now(clock);
        AccountTextAiSession session = AccountTextAiSession.builder()
                .account(account)
                .openAiThreadId(null)
                .title(request.title().trim())
                .resume(request.resume().trim())
                .timeConsidered(Boolean.TRUE.equals(request.time()))
                .expectedMinutes(request.expected())
                .createdAt(now)
                .lastUpdatedAt(null)
                .build();
        session = sessionRepositoryJpa.save(session);

        String userJson = buildOpenAiUserPayloadJson(
                null,
                request.title().trim(),
                request.resume().trim(),
                request.resumeModification(),
                Boolean.TRUE.equals(request.time()),
                request.expected(),
                request.chat()
        );
        OpenAiAssistantThreadTurnResult turn = openAiAssistantThreadTurnPort.runTurn(
                openAiProperties.getAssistantTextAiId().trim(),
                null,
                userJson
        );
        TextAiAssistantAiModelResponse parsed = parseModelResponse(turn.assistantMessageText());
        validateParsedModelResponse(parsed);

        session.setOpenAiThreadId(turn.openAiThreadId());
        applyResumeFromModelIfModification(session, parsed);
        sessionRepositoryJpa.save(session);

        return toApiResponse(turn.openAiThreadId(), parsed);
    }

    /**
     * Volta seguinte: o pedido inclui {@code thread_id} (id da thread na OpenAI), portanto a sessão local já foi
     * criada na primeira volta e tem {@link AccountTextAiSession#getId() textAiSessionId} e
     * {@link AccountTextAiSession#getOpenAiThreadId() openai_thread_id} persistidos. Na primeira interação esse
     * id ainda não existe — a validação abaixo só corre neste ramo.
     */
    private TextAiAssistantResponse processContinuingTurn(Account account, TextAiAssistantRequest request) {
        String openAiThreadId = request.threadId().trim();
        AccountTextAiSession session = loadPersistedSessionForContinuingOpenAiTurnOrThrow(account.getId(), openAiThreadId);

        applyOptionalSessionUpdatesFromRequest(session, request);
        session.setLastUpdatedAt(LocalDateTime.now(clock));
        sessionRepositoryJpa.save(session);

        String userJson = buildOpenAiUserPayloadJson(
                openAiThreadId,
                session.getTitle(),
                session.getResume(),
                request.resumeModification(),
                session.isTimeConsidered(),
                session.getExpectedMinutes(),
                request.chat()
        );
        OpenAiAssistantThreadTurnResult turn = openAiAssistantThreadTurnPort.runTurn(
                openAiProperties.getAssistantTextAiId().trim(),
                openAiThreadId,
                userJson
        );
        TextAiAssistantAiModelResponse parsed = parseModelResponse(turn.assistantMessageText());
        validateParsedModelResponse(parsed);

        applyResumeFromModelIfModification(session, parsed);
        session.setLastUpdatedAt(LocalDateTime.now(clock));
        sessionRepositoryJpa.save(session);

        return toApiResponse(turn.openAiThreadId(), parsed);
    }

    /**
     * Resolve a sessão pela combinação conta + {@code openAiThreadId} e impede continuidade quando
     * {@link AccountTextAiSession#getIsDeleted()} é true, antes de qualquer chamada ao assistente.
     */
    private AccountTextAiSession loadPersistedSessionForContinuingOpenAiTurnOrThrow(
            Long accountId,
            String openAiThreadId
    ) {
        AccountTextAiSession session = sessionRepositoryJpa
                .findByAccount_IdAndOpenAiThreadId(accountId, openAiThreadId)
                .orElseThrow(() -> new AccountException(TextAiAssistantValidationConstants.MESSAGE_SESSION_NOT_FOUND_FOR_THREAD));
        if (Boolean.TRUE.equals(session.getIsDeleted())) {
            throw new AccountException(TextAiAssistantValidationConstants.MESSAGE_TEXT_AI_SESSION_DELETED_CANNOT_CALL_ASSISTANT);
        }
        return session;
    }

    private static TextAiAssistantResponse toApiResponse(
            String openAiThreadId,
            TextAiAssistantAiModelResponse parsed
    ) {
        return new TextAiAssistantResponse(
                openAiThreadId,
                parsed.title(),
                parsed.allResume(),
                parsed.modificationResume(),
                Boolean.TRUE.equals(parsed.modification()),
                parsed.chat()
        );
    }

    private void applyResumeFromModelIfModification(
            AccountTextAiSession session,
            TextAiAssistantAiModelResponse parsed
    ) {
        if (!Boolean.TRUE.equals(parsed.modification())) {
            return;
        }
        if (parsed.allResume() == null) {
            return;
        }
        session.setResume(parsed.allResume());
    }

    private static void validateParsedModelResponse(TextAiAssistantAiModelResponse parsed) {
        if (parsed.title() == null || parsed.title().isBlank()
                || parsed.chat() == null || parsed.chat().isBlank()
                || parsed.modification() == null) {
            throw new AccountException(TextAiAssistantValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
        }
    }

    private static void validateFirstTurnRequest(TextAiAssistantRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new AccountException(TextAiAssistantValidationConstants.MESSAGE_FIRST_TURN_TITLE_REQUIRED);
        }
        if (request.resume() == null || request.resume().isBlank()) {
            throw new AccountException(TextAiAssistantValidationConstants.MESSAGE_FIRST_TURN_RESUME_REQUIRED);
        }
        if (request.time() == null) {
            throw new AccountException(TextAiAssistantValidationConstants.MESSAGE_FIRST_TURN_TIME_REQUIRED);
        }
        if (!isExpectedMinutesInAllowedRange(request.expected())) {
            throw new AccountException(TextAiAssistantValidationConstants.MESSAGE_FIRST_TURN_EXPECTED_INVALID);
        }
    }

    private static boolean isExpectedMinutesInAllowedRange(Integer expectedMinutes) {
        if (expectedMinutes == null) {
            return false;
        }
        int value = expectedMinutes;
        return value >= TextAiAssistantValidationConstants.MIN_EXPECTED_MINUTES
                && value <= TextAiAssistantValidationConstants.MAX_EXPECTED_MINUTES;
    }

    private void applyOptionalSessionUpdatesFromRequest(
            AccountTextAiSession session,
            TextAiAssistantRequest request
    ) {
        if (request.title() != null && !request.title().isBlank()) {
            session.setTitle(request.title().trim());
        }
        if (request.resume() != null && !request.resume().isBlank()) {
            session.setResume(request.resume().trim());
        }
        if (request.time() != null) {
            session.setTimeConsidered(Boolean.TRUE.equals(request.time()));
        }
        if (request.expected() != null) {
            if (!isExpectedMinutesInAllowedRange(request.expected())) {
                throw new AccountException(
                        TextAiAssistantValidationConstants.MESSAGE_EXPECTED_MINUTES_OUT_OF_RANGE_WHEN_PROVIDED
                );
            }
            session.setExpectedMinutes(request.expected());
        }
    }

    private TextAiAssistantAiModelResponse parseModelResponse(String rawAssistantText) {
        try {
            String sanitized = sanitizeAssistantJsonPayload(rawAssistantText);
            return objectMapper.readValue(sanitized, TextAiAssistantAiModelResponse.class);
        } catch (Exception ex) {
            log.warn("text ai assistant: resposta do modelo inválida: {}", ex.getMessage());
            throw new AccountException(TextAiAssistantValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON, ex);
        }
    }

    private static String sanitizeAssistantJsonPayload(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (!trimmed.startsWith(TextAiAssistantContentSanitizerConstants.MARKDOWN_CODE_FENCE)) {
            return trimmed;
        }
        int firstNewline = trimmed.indexOf('\n');
        if (firstNewline > 0) {
            trimmed = trimmed.substring(firstNewline + 1);
        }
        if (trimmed.endsWith(TextAiAssistantContentSanitizerConstants.MARKDOWN_CODE_FENCE)) {
            trimmed = trimmed.substring(
                    0,
                    trimmed.length() - TextAiAssistantContentSanitizerConstants.MARKDOWN_CODE_FENCE.length()
            ).trim();
        }
        return trimmed;
    }

    private String buildOpenAiUserPayloadJson(
            String openAiThreadIdOrNull,
            String title,
            String resume,
            String resumeModificationOrNull,
            boolean timeConsidered,
            int expectedMinutes,
            String chat
    ) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            if (openAiThreadIdOrNull != null && !openAiThreadIdOrNull.isBlank()) {
                node.put(TextAiAssistantOpenAiUserPayloadJsonConstants.THREAD_ID, openAiThreadIdOrNull);
            } else {
                node.putNull(TextAiAssistantOpenAiUserPayloadJsonConstants.THREAD_ID);
            }
            node.put(TextAiAssistantOpenAiUserPayloadJsonConstants.TITLE, title);
            node.put(TextAiAssistantOpenAiUserPayloadJsonConstants.RESUME, resume);
            if (resumeModificationOrNull != null && !resumeModificationOrNull.isBlank()) {
                node.put(
                        TextAiAssistantOpenAiUserPayloadJsonConstants.RESUME_MODIFICATION,
                        resumeModificationOrNull.trim()
                );
            } else {
                node.putNull(TextAiAssistantOpenAiUserPayloadJsonConstants.RESUME_MODIFICATION);
            }
            node.put(TextAiAssistantOpenAiUserPayloadJsonConstants.TIME, timeConsidered);
            node.put(TextAiAssistantOpenAiUserPayloadJsonConstants.EXPECTED, expectedMinutes);
            node.put(TextAiAssistantOpenAiUserPayloadJsonConstants.CHAT, chat.trim());
            return objectMapper.writeValueAsString(node);
        } catch (Exception ex) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED, ex);
        }
    }

    private Account getAccountOrThrowAndAssertActive(Long accountId) {
        Account account = accountRepositoryJpa.findById(accountId)
                .orElseThrow(() -> new AccountException(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
        if (account.getStatus() != AccountStatusEnum.ACTIVE) {
            log.warn(TopicServiceLogConstants.TOPIC_ENDPOINT_REJECTED_ACCOUNT_NOT_ACTIVE);
            throw new AccountException(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS);
        }
        return account;
    }
}
