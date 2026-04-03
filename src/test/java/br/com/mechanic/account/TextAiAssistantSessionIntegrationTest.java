package br.com.mechanic.account;

import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.TextAiAssistantValidationConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.textai.AccountTextAiSession;
import br.com.mechanic.account.repository.account.jpa.AccountRepositoryJpa;
import br.com.mechanic.account.repository.textai.jpa.AccountTextAiSessionHistoryRepositoryJpa;
import br.com.mechanic.account.repository.textai.jpa.AccountTextAiSessionRepositoryJpa;
import br.com.mechanic.account.service.openai.OpenAiAssistantThreadTurnPort;
import br.com.mechanic.account.service.openai.OpenAiAssistantThreadTurnResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TextAiAssistantSessionIntegrationTest.StubThreadTurnConfiguration.class)
class TextAiAssistantSessionIntegrationTest {

    private static final String PASSWORD_VALID = "secret123";

    private static final String STUB_THREAD = "thread_sess_integration";

    private static final String AI_STUB = """
            {"title":"S","allResume":"R","modificationResume":null,"modification":false,"chat":"ok"}
            """;

    @TestConfiguration
    static class StubThreadTurnConfiguration {

        @Bean
        @Primary
        OpenAiAssistantThreadTurnPort stubOpenAiAssistantThreadTurnPort() {
            return (assistantId, existingOpenAiThreadId, userMessageJson) ->
                    new OpenAiAssistantThreadTurnResult(STUB_THREAD, AI_STUB.trim());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountTextAiSessionRepositoryJpa sessionRepositoryJpa;

    @Autowired
    private AccountTextAiSessionHistoryRepositoryJpa sessionHistoryRepositoryJpa;

    @Autowired
    private AccountRepositoryJpa accountRepositoryJpa;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET sessions paginado: retorna sessões da conta ordenadas")
    void listSessionsPaginated() throws Exception {
        Long accountId = createAccountAndGetId("sess-list-" + UUID.randomUUID() + "@t.com");
        linkProfileForTextAiAccess(accountId);

        Account account = accountRepositoryJpa.findById(accountId).orElseThrow();
        LocalDateTime t0 = LocalDateTime.of(2026, 4, 1, 10, 0);
        LocalDateTime t1 = LocalDateTime.of(2026, 4, 2, 10, 0);
        persistSession(account, "t-old", t0);
        persistSession(account, "t-new", t1);
        persistDeletedSession(account, "t-del", LocalDateTime.of(2026, 4, 3, 12, 0));

        mockMvc.perform(
                        get(sessionsPath(accountId))
                                .param("page", "0")
                                .param("size", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].openAiThreadId").value("t-new"));
    }

    @Test
    @DisplayName("GET session por id: retorna recurso quando pertence à conta")
    void getByIdWhenOwned() throws Exception {
        Long accountId = createAccountAndGetId("sess-one-" + UUID.randomUUID() + "@t.com");
        linkProfileForTextAiAccess(accountId);
        Account account = accountRepositoryJpa.findById(accountId).orElseThrow();
        AccountTextAiSession saved = persistSession(account, "thread-owned", LocalDateTime.of(2026, 4, 3, 8, 0));

        mockMvc.perform(get(sessionByIdPath(accountId, saved.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.openAiThreadId").value("thread-owned"));
    }

    @Test
    @DisplayName("GET session por id: 400 quando id não existe para a conta")
    void getByIdWhenNotFoundReturnsBadRequest() throws Exception {
        Long accountId = createAccountAndGetId("sess-404-" + UUID.randomUUID() + "@t.com");
        linkProfileForTextAiAccess(accountId);

        mockMvc.perform(get(sessionByIdPath(accountId, 999_999L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TextAiAssistantValidationConstants.MESSAGE_TEXT_AI_SESSION_NOT_FOUND_FOR_ACCOUNT));
    }

    @Test
    @DisplayName("GET session por id: 400 quando sessão é de outra conta")
    void getByIdWhenWrongAccountReturnsBadRequest() throws Exception {
        Long ownerA = createAccountAndGetId("sess-a-" + UUID.randomUUID() + "@t.com");
        linkProfileForTextAiAccess(ownerA);
        Long ownerB = createAccountAndGetId("sess-b-" + UUID.randomUUID() + "@t.com");
        linkProfileForTextAiAccess(ownerB);

        Account accountA = accountRepositoryJpa.findById(ownerA).orElseThrow();
        AccountTextAiSession saved = persistSession(accountA, "only-a", LocalDateTime.now());

        mockMvc.perform(get(sessionByIdPath(ownerB, saved.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TextAiAssistantValidationConstants.MESSAGE_TEXT_AI_SESSION_NOT_FOUND_FOR_ACCOUNT));
    }

    @Test
    @DisplayName("GET session por id: 400 quando sessão está soft-deleted")
    void getByIdWhenSoftDeletedReturnsBadRequest() throws Exception {
        Long accountId = createAccountAndGetId("sess-delget-" + UUID.randomUUID() + "@t.com");
        linkProfileForTextAiAccess(accountId);
        Account account = accountRepositoryJpa.findById(accountId).orElseThrow();
        AccountTextAiSession removed = persistDeletedSession(account, "gone", LocalDateTime.of(2026, 4, 2, 15, 0));

        mockMvc.perform(get(sessionByIdPath(accountId, removed.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TextAiAssistantValidationConstants.MESSAGE_TEXT_AI_SESSION_NOT_FOUND_FOR_ACCOUNT));
    }

    @Test
    @DisplayName("DELETE session: soft delete, histórico gravado e segunda chamada como inexistente")
    void deleteSessionSoftDeletesAndWritesHistory() throws Exception {
        Long accountId = createAccountAndGetId("sess-del-" + UUID.randomUUID() + "@t.com");
        linkProfileForTextAiAccess(accountId);
        Account account = accountRepositoryJpa.findById(accountId).orElseThrow();
        AccountTextAiSession saved = persistSession(account, "to-delete", LocalDateTime.of(2026, 4, 3, 9, 0));
        assertEquals(0L, sessionHistoryRepositoryJpa.count());

        mockMvc.perform(delete(sessionByIdPath(accountId, saved.getId())))
                .andExpect(status().isNoContent());

        AccountTextAiSession reloaded = sessionRepositoryJpa.findById(saved.getId()).orElseThrow();
        assertTrue(Boolean.TRUE.equals(reloaded.getIsDeleted()));
        assertEquals(1L, sessionHistoryRepositoryJpa.count());

        mockMvc.perform(delete(sessionByIdPath(accountId, saved.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TextAiAssistantValidationConstants.MESSAGE_TEXT_AI_SESSION_NOT_FOUND_FOR_ACCOUNT));
    }

    private AccountTextAiSession persistSession(Account account, String threadId, LocalDateTime createdAt) {
        AccountTextAiSession session = AccountTextAiSession.builder()
                .account(account)
                .openAiThreadId(threadId)
                .title("T")
                .resume("R")
                .timeConsidered(true)
                .expectedMinutes(10)
                .createdAt(createdAt)
                .lastUpdatedAt(null)
                .build();
        return sessionRepositoryJpa.save(session);
    }

    private AccountTextAiSession persistDeletedSession(Account account, String threadId, LocalDateTime createdAt) {
        AccountTextAiSession session = AccountTextAiSession.builder()
                .account(account)
                .openAiThreadId(threadId)
                .title("T")
                .resume("R")
                .timeConsidered(true)
                .expectedMinutes(10)
                .createdAt(createdAt)
                .lastUpdatedAt(createdAt.plusMinutes(5))
                .isDeleted(Boolean.TRUE)
                .build();
        return sessionRepositoryJpa.save(session);
    }

    private void linkProfileForTextAiAccess(Long accountId) throws Exception {
        mockMvc.perform(
                        post(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.ACCOUNT_PROFILES_SEGMENT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"profileType\":\"BISHOP\"}")
                )
                .andExpect(status().isCreated());
    }

    private Long createAccountAndGetId(String email) throws Exception {
        String body = """
                {
                  "email": "%s",
                  "password": "%s",
                  "confirmPassword": "%s",
                  "firstName": "User",
                  "lastName": "Test",
                  "birthDate": "%s"
                }
                """.formatted(email, PASSWORD_VALID, PASSWORD_VALID, LocalDate.now().minusYears(35));

        String response = mockMvc.perform(post(ApiPathConstants.ACCOUNTS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("id").asLong();
    }

    private static String sessionsPath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.TEXT_AI_ASSISTANT_SESSIONS_SEGMENT;
    }

    private static String sessionByIdPath(Long accountId, Long sessionId) {
        return sessionsPath(accountId) + "/" + sessionId;
    }
}
