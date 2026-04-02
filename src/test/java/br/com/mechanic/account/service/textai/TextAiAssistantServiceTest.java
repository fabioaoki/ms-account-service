package br.com.mechanic.account.service.textai;

import br.com.mechanic.account.config.OpenAiProperties;
import br.com.mechanic.account.constant.TextAiAssistantOpenAiUserPayloadJsonConstants;
import br.com.mechanic.account.constant.TextAiAssistantValidationConstants;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TextAiAssistantServiceTest {

    private static final String AI_JSON_MODIFICATION_TRUE = """
            {"title":"T reformulado","allResume":"TEXTO IA COMPLETO","modificationResume":null,"modification":true,"chat":"Posso ajudar?"}
            """;

    private static final String AI_JSON_MODIFICATION_FALSE = """
            {"title":"T","allResume":"ignorado","modificationResume":null,"modification":false,"chat":"Só dica."}
            """;

    @Mock
    private ApiAccessValidation apiAccessValidation;

    @Mock
    private AccountRepositoryJpa accountRepositoryJpa;

    @Mock
    private AccountTextAiSessionRepositoryJpa sessionRepositoryJpa;

    @Mock
    private OpenAiAssistantThreadTurnPort openAiAssistantThreadTurnPort;

    @Mock
    private OpenAiProperties openAiProperties;

    private TextAiAssistantService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Clock clock = Clock.fixed(Instant.parse("2026-03-31T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new TextAiAssistantService(
                apiAccessValidation,
                accountRepositoryJpa,
                sessionRepositoryJpa,
                openAiAssistantThreadTurnPort,
                openAiProperties,
                objectMapper,
                clock
        );
        doNothing().when(apiAccessValidation).requireTextAiAssistantAccess(any());
        when(openAiProperties.usesAssistantTextAi()).thenReturn(true);
        when(openAiProperties.getAssistantTextAiId()).thenReturn("asst_unit_test");
    }

    @Test
    @DisplayName("Primeira volta: modification=true substitui resume persistido por allResume")
    void firstTurnModificationTrueReplacesResume() {
        long accountId = 9L;
        Account account = Account.builder()
                .id(accountId)
                .publicId(UUID.randomUUID())
                .status(AccountStatusEnum.ACTIVE)
                .build();
        when(accountRepositoryJpa.findById(accountId)).thenReturn(Optional.of(account));

        when(sessionRepositoryJpa.save(any(AccountTextAiSession.class))).thenAnswer(inv -> {
            AccountTextAiSession s = inv.getArgument(0);
            if (s.getId() == null) {
                s.setId(100L);
            }
            return s;
        });

        when(openAiAssistantThreadTurnPort.runTurn(eq("asst_unit_test"), eq(null), any()))
                .thenReturn(new OpenAiAssistantThreadTurnResult("thread_openai_1", AI_JSON_MODIFICATION_TRUE.trim()));

        TextAiAssistantRequest req = new TextAiAssistantRequest(
                null,
                "T",
                "usuario",
                null,
                Boolean.TRUE,
                Integer.valueOf(60),
                "chat"
        );

        TextAiAssistantResponse response = service.process(accountId, req);

        assertEquals("thread_openai_1", response.threadId());
        assertEquals("T reformulado", response.title());
        assertEquals("TEXTO IA COMPLETO", response.allResume());
        assertEquals(true, response.modification());

        ArgumentCaptor<AccountTextAiSession> captor = ArgumentCaptor.forClass(AccountTextAiSession.class);
        verify(sessionRepositoryJpa, org.mockito.Mockito.atLeast(2)).save(captor.capture());
        AccountTextAiSession last = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals("thread_openai_1", last.getOpenAiThreadId());
        assertEquals("TEXTO IA COMPLETO", last.getResume());
        assertNull(last.getLastUpdatedAt());
    }

    @Test
    @DisplayName("Primeira volta: modification=false mantém resume do utilizador")
    void firstTurnModificationFalseKeepsUserResume() {
        long accountId = 11L;
        Account account = Account.builder()
                .id(accountId)
                .publicId(UUID.randomUUID())
                .status(AccountStatusEnum.ACTIVE)
                .build();
        when(accountRepositoryJpa.findById(accountId)).thenReturn(Optional.of(account));

        when(sessionRepositoryJpa.save(any(AccountTextAiSession.class))).thenAnswer(inv -> {
            AccountTextAiSession s = inv.getArgument(0);
            if (s.getId() == null) {
                s.setId(200L);
            }
            return s;
        });

        when(openAiAssistantThreadTurnPort.runTurn(eq("asst_unit_test"), eq(null), any()))
                .thenReturn(new OpenAiAssistantThreadTurnResult("thread_x", AI_JSON_MODIFICATION_FALSE.trim()));

        TextAiAssistantRequest req = new TextAiAssistantRequest(
                null,
                "T",
                "meu texto",
                null,
                Boolean.FALSE,
                Integer.valueOf(30),
                "pergunta"
        );

        service.process(accountId, req);

        ArgumentCaptor<AccountTextAiSession> captor = ArgumentCaptor.forClass(AccountTextAiSession.class);
        verify(sessionRepositoryJpa, org.mockito.Mockito.atLeast(2)).save(captor.capture());
        AccountTextAiSession last = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals("meu texto", last.getResume());
    }

    @Test
    @DisplayName("Segunda volta: rejeita thread desconhecido")
    void continuingTurnUsesExistingThreadOrThrows() {
        long accountId = 3L;
        Account account = Account.builder()
                .id(accountId)
                .publicId(UUID.randomUUID())
                .status(AccountStatusEnum.ACTIVE)
                .build();
        when(accountRepositoryJpa.findById(accountId)).thenReturn(Optional.of(account));

        when(sessionRepositoryJpa.findByAccount_IdAndOpenAiThreadId(accountId, "bad"))
                .thenReturn(Optional.empty());

        TextAiAssistantRequest bad = new TextAiAssistantRequest(
                "bad",
                "T",
                "r",
                null,
                Boolean.TRUE,
                Integer.valueOf(45),
                "c"
        );

        AccountException ex = assertThrows(AccountException.class, () -> service.process(accountId, bad));
        assertEquals(TextAiAssistantValidationConstants.MESSAGE_SESSION_NOT_FOUND_FOR_THREAD, ex.getMessage());
        verify(openAiAssistantThreadTurnPort, never()).runTurn(any(), any(), any());
    }

    @Test
    @DisplayName("Assistente não configurado: lança exceção")
    void whenTextAiAssistantNotConfiguredThrows() {
        when(openAiProperties.usesAssistantTextAi()).thenReturn(false);

        AccountException ex = assertThrows(
                AccountException.class,
                () -> service.process(
                        1L,
                        new TextAiAssistantRequest(
                                null,
                                "t",
                                "r",
                                null,
                                Boolean.TRUE,
                                Integer.valueOf(10),
                                "c"
                        )
                )
        );
        assertEquals(TextAiAssistantValidationConstants.MESSAGE_ASSISTANT_TEXT_AI_NOT_CONFIGURED, ex.getMessage());
    }

    @Test
    @DisplayName("Segunda volta: payload OpenAI usa dados da sessão quando campos opcionais omitidos")
    void continuingTurnUsesSessionWhenOptionalFieldsOmitted() throws Exception {
        long accountId = 7L;
        Account account = Account.builder()
                .id(accountId)
                .publicId(UUID.randomUUID())
                .status(AccountStatusEnum.ACTIVE)
                .build();
        when(accountRepositoryJpa.findById(accountId)).thenReturn(Optional.of(account));

        AccountTextAiSession session = AccountTextAiSession.builder()
                .id(501L)
                .account(account)
                .openAiThreadId("thread_merge")
                .title("Título salvo")
                .resume("Resumo salvo")
                .timeConsidered(true)
                .expectedMinutes(55)
                .createdAt(LocalDateTime.now(clock))
                .lastUpdatedAt(null)
                .build();
        when(sessionRepositoryJpa.findByAccount_IdAndOpenAiThreadId(accountId, "thread_merge"))
                .thenReturn(Optional.of(session));

        when(sessionRepositoryJpa.save(any(AccountTextAiSession.class))).thenAnswer(inv -> inv.getArgument(0));

        when(openAiAssistantThreadTurnPort.runTurn(eq("asst_unit_test"), eq("thread_merge"), any()))
                .thenReturn(new OpenAiAssistantThreadTurnResult("thread_merge", AI_JSON_MODIFICATION_FALSE.trim()));

        TextAiAssistantRequest req = new TextAiAssistantRequest(
                "thread_merge",
                null,
                null,
                null,
                null,
                null,
                "Só uma pergunta"
        );

        service.process(accountId, req);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(openAiAssistantThreadTurnPort).runTurn(eq("asst_unit_test"), eq("thread_merge"), jsonCaptor.capture());
        JsonNode node = objectMapper.readTree(jsonCaptor.getValue());
        assertEquals("Título salvo", node.get(TextAiAssistantOpenAiUserPayloadJsonConstants.TITLE).asText());
        assertEquals("Resumo salvo", node.get(TextAiAssistantOpenAiUserPayloadJsonConstants.RESUME).asText());
        assertEquals(true, node.get(TextAiAssistantOpenAiUserPayloadJsonConstants.TIME).asBoolean());
        assertEquals(55, node.get(TextAiAssistantOpenAiUserPayloadJsonConstants.EXPECTED).asInt());
        assertEquals("Só uma pergunta", node.get(TextAiAssistantOpenAiUserPayloadJsonConstants.CHAT).asText());
    }
}
