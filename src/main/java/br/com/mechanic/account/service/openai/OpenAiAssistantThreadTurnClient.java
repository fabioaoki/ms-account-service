package br.com.mechanic.account.service.openai;

import br.com.mechanic.account.config.OpenAiProperties;
import br.com.mechanic.account.constant.OpenAiAssistantsApiConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.exception.AccountException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Volta de conversa na API Assistants com thread persistente (não elimina a thread ao fim).
 */
public class OpenAiAssistantThreadTurnClient implements OpenAiAssistantThreadTurnPort {

    private final RestClient restClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiAssistantThreadTurnClient(OpenAiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.getResolvedBaseUrlHost())
                .defaultHeader(
                        OpenAiAssistantsApiConstants.HEADER_OPENAI_BETA,
                        OpenAiAssistantsApiConstants.HEADER_VALUE_ASSISTANTS_V2
                );
        if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey());
        }
        this.restClient = builder.build();
    }

    @Override
    public OpenAiAssistantThreadTurnResult runTurn(
            String assistantId,
            @Nullable String existingOpenAiThreadId,
            String userMessageJson
    ) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_API_KEY_NOT_CONFIGURED);
        }
        String trimmedAssistantId = assistantId == null ? "" : assistantId.trim();
        if (trimmedAssistantId.isEmpty()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED);
        }
        try {
            String threadId = existingOpenAiThreadId == null || existingOpenAiThreadId.isBlank()
                    ? createThread()
                    : existingOpenAiThreadId.trim();
            addUserMessage(threadId, userMessageJson);
            String runId = createRun(threadId, trimmedAssistantId);
            waitForRunSuccess(threadId, runId);
            String text = fetchLatestAssistantText(threadId);
            return new OpenAiAssistantThreadTurnResult(threadId, text);
        } catch (AccountException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_ASSISTANT_POLL_TIMEOUT, ex);
        } catch (Exception ex) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED, ex);
        }
    }

    private String createThread() throws Exception {
        String raw = restClient.post()
                .uri(OpenAiAssistantsApiConstants.THREADS_RESOURCE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .retrieve()
                .body(String.class);
        if (raw == null || raw.isBlank()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED);
        }
        JsonNode root = objectMapper.readTree(raw);
        String id = root.path(OpenAiAssistantsApiConstants.JSON_FIELD_ID).asText(null);
        if (id == null || id.isBlank()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED);
        }
        return id;
    }

    private void addUserMessage(String threadId, String userMessageJson) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put(OpenAiAssistantsApiConstants.JSON_BODY_KEY_ROLE, OpenAiAssistantsApiConstants.MESSAGE_ROLE_USER);
        body.put(OpenAiAssistantsApiConstants.JSON_BODY_KEY_CONTENT, userMessageJson);
        String path = OpenAiAssistantsApiConstants.buildThreadMessagesPath(threadId);
        restClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(body))
                .retrieve()
                .toBodilessEntity();
    }

    private String createRun(String threadId, String assistantIdValue) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put(OpenAiAssistantsApiConstants.JSON_BODY_KEY_ASSISTANT_ID, assistantIdValue);
        String path = OpenAiAssistantsApiConstants.buildThreadRunsPath(threadId);
        ResponseEntity<String> response = restClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(body))
                .retrieve()
                .toEntity(String.class);
        String raw = response.getBody();
        if (raw == null || raw.isBlank()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED);
        }
        JsonNode root = objectMapper.readTree(raw);
        String id = root.path(OpenAiAssistantsApiConstants.JSON_FIELD_ID).asText(null);
        if (id == null || id.isBlank()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED);
        }
        return id;
    }

    private void waitForRunSuccess(String threadId, String runId) throws Exception {
        for (int attempt = 0; attempt < OpenAiAssistantsApiConstants.RUN_POLL_MAX_ATTEMPTS; attempt++) {
            String path = OpenAiAssistantsApiConstants.buildThreadRunStatusPath(threadId, runId);
            ResponseEntity<String> response = restClient.get()
                    .uri(path)
                    .retrieve()
                    .toEntity(String.class);
            String raw = response.getBody();
            if (raw == null || raw.isBlank()) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED);
            }
            JsonNode root = objectMapper.readTree(raw);
            String status = root.path(OpenAiAssistantsApiConstants.JSON_FIELD_STATUS).asText("");
            if (OpenAiAssistantsApiConstants.RUN_STATUS_COMPLETED.equals(status)) {
                return;
            }
            if (isRunFailedTerminalStatus(status)) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_ASSISTANT_RUN_FAILED);
            }
            Thread.sleep(OpenAiAssistantsApiConstants.RUN_POLL_INTERVAL_MILLIS);
        }
        throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_ASSISTANT_POLL_TIMEOUT);
    }

    private static boolean isRunFailedTerminalStatus(String status) {
        return OpenAiAssistantsApiConstants.RUN_STATUS_FAILED.equals(status)
                || OpenAiAssistantsApiConstants.RUN_STATUS_CANCELLED.equals(status)
                || OpenAiAssistantsApiConstants.RUN_STATUS_EXPIRED.equals(status)
                || OpenAiAssistantsApiConstants.RUN_STATUS_INCOMPLETE.equals(status)
                || OpenAiAssistantsApiConstants.RUN_STATUS_REQUIRES_ACTION.equals(status);
    }

    private String fetchLatestAssistantText(String threadId) throws Exception {
        String path = OpenAiAssistantsApiConstants.buildThreadMessagesPath(threadId);
        String raw = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam(OpenAiAssistantsApiConstants.QUERY_PARAM_ORDER, OpenAiAssistantsApiConstants.MESSAGES_ORDER_DESC)
                        .queryParam(
                                OpenAiAssistantsApiConstants.QUERY_PARAM_LIMIT,
                                OpenAiAssistantsApiConstants.MESSAGES_LIST_LIMIT
                        )
                        .build())
                .retrieve()
                .body(String.class);
        if (raw == null || raw.isBlank()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED);
        }
        JsonNode root = objectMapper.readTree(raw);
        JsonNode data = root.path(OpenAiAssistantsApiConstants.JSON_FIELD_DATA);
        if (!data.isArray()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED);
        }
        for (JsonNode message : data) {
            String role = message.path(OpenAiAssistantsApiConstants.JSON_FIELD_ROLE).asText("");
            if (OpenAiAssistantsApiConstants.MESSAGE_ROLE_ASSISTANT.equals(role)) {
                String text = extractMessageTextContent(message.path(OpenAiAssistantsApiConstants.JSON_FIELD_CONTENT));
                if (text != null && !text.isBlank()) {
                    return text.trim();
                }
            }
        }
        throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED);
    }

    private String extractMessageTextContent(JsonNode contentNode) {
        if (contentNode == null || contentNode.isNull()) {
            return null;
        }
        if (contentNode.isTextual()) {
            return contentNode.asText();
        }
        if (!contentNode.isArray()) {
            return null;
        }
        StringBuilder buffer = new StringBuilder();
        for (JsonNode part : contentNode) {
            if (!OpenAiAssistantsApiConstants.CONTENT_TYPE_TEXT.equals(
                    part.path(OpenAiAssistantsApiConstants.JSON_FIELD_TYPE).asText("")
            )) {
                continue;
            }
            JsonNode textObj = part.path(OpenAiAssistantsApiConstants.JSON_FIELD_TEXT);
            if (textObj.isTextual()) {
                buffer.append(textObj.asText());
                continue;
            }
            if (textObj.isObject()) {
                buffer.append(textObj.path(OpenAiAssistantsApiConstants.JSON_FIELD_VALUE).asText(""));
            }
        }
        return buffer.length() == 0 ? null : buffer.toString();
    }
}
