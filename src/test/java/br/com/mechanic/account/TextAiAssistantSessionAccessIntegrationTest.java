package br.com.mechanic.account;

import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.AuthValidationConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.textai.AccountTextAiSession;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import br.com.mechanic.account.repository.account.jpa.AccountRepositoryJpa;
import br.com.mechanic.account.repository.textai.jpa.AccountTextAiSessionRepositoryJpa;
import br.com.mechanic.account.security.JwtTestAuthentication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.security.integration-test-disable-jwt=false")
class TextAiAssistantSessionAccessIntegrationTest {

    private static final String PASSWORD_VALID = "secret123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepositoryJpa accountRepositoryJpa;

    @Autowired
    private AccountTextAiSessionRepositoryJpa sessionRepositoryJpa;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET sessions: sem perfil BISHOP (TextAiAssistantAccessConstants) retorna 403")
    void listSessionsWithoutAllowedProfileReturnsForbidden() throws Exception {
        long accountId = createAccountAndGetId("ta-sess-403-" + UUID.randomUUID() + "@t.com");

        mockMvc.perform(
                        get(sessionsPath(accountId))
                                .with(JwtTestAuthentication.annotatorOnly(accountId))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(AuthValidationConstants.MESSAGE_ACCESS_DENIED));
    }

    @Test
    @DisplayName("DELETE session: sem perfil autorizado retorna 403")
    void deleteWithoutAllowedProfileReturnsForbidden() throws Exception {
        long accountId = createAccountAndGetId("ta-del-403-" + UUID.randomUUID() + "@t.com");

        AccountTextAiSession session = persistSessionBare(accountId);

        mockMvc.perform(
                        delete(sessionByIdPath(accountId, session.getId()))
                                .with(JwtTestAuthentication.annotatorOnly(accountId))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(AuthValidationConstants.MESSAGE_ACCESS_DENIED));
    }

    @Test
    @DisplayName("GET sessions: conta INACTIVE retorna 400 (mesma regra do POST de consulta)")
    void listSessionsWhenAccountInactiveReturnsBadRequest() throws Exception {
        long accountId = createAccountAndGetId("ta-inact-list-" + UUID.randomUUID() + "@t.com");
        linkBishopProfile(accountId);
        deactivateAsOwnerStandard(accountId);

        mockMvc.perform(
                        get(sessionsPath(accountId))
                                .with(JwtTestAuthentication.annotatorOnly(accountId))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS));
    }

    @Test
    @DisplayName("DELETE session: conta INACTIVE retorna 400")
    void deleteWhenAccountInactiveReturnsBadRequest() throws Exception {
        long accountId = createAccountAndGetId("ta-inact-del-" + UUID.randomUUID() + "@t.com");
        linkBishopProfile(accountId);
        AccountTextAiSession session = persistSessionBare(accountId);
        deactivateAsOwnerStandard(accountId);

        mockMvc.perform(
                        delete(sessionByIdPath(accountId, session.getId()))
                                .with(JwtTestAuthentication.annotatorOnly(accountId))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS));
    }

    @Test
    @DisplayName("GET sessions: com BISHOP vinculado e JWT da própria conta retorna 200")
    void listSessionsWithAllowedProfileReturnsOk() throws Exception {
        long accountId = createAccountAndGetId("ta-ok-list-" + UUID.randomUUID() + "@t.com");
        linkBishopProfile(accountId);

        mockMvc.perform(
                        get(sessionsPath(accountId))
                                .with(JwtTestAuthentication.annotatorOnly(accountId))
                )
                .andExpect(status().isOk());
    }

    private void deactivateAsOwnerStandard(long accountId) throws Exception {
        mockMvc.perform(
                        patch(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.ACCOUNT_DEACTIVATE_SEGMENT)
                                .with(JwtTestAuthentication.ownerStandard(accountId))
                )
                .andExpect(status().isOk());
        Account reloaded = accountRepositoryJpa.findById(accountId).orElseThrow();
        assertEquals(AccountStatusEnum.INACTIVE, reloaded.getStatus());
    }

    private AccountTextAiSession persistSessionBare(long accountId) {
        Account account = accountRepositoryJpa.findById(accountId).orElseThrow();
        AccountTextAiSession session = AccountTextAiSession.builder()
                .account(account)
                .openAiThreadId("sec-" + UUID.randomUUID())
                .title("T")
                .resume("R")
                .timeConsidered(true)
                .expectedMinutes(10)
                .createdAt(LocalDateTime.now())
                .lastUpdatedAt(null)
                .isDeleted(null)
                .build();
        return sessionRepositoryJpa.save(session);
    }

    private void linkBishopProfile(long accountId) throws Exception {
        mockMvc.perform(
                        post(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.ACCOUNT_PROFILES_SEGMENT)
                                .with(JwtTestAuthentication.annotatorOnly(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"profileType\":\"BISHOP\"}")
                )
                .andExpect(status().isCreated());
    }

    private long createAccountAndGetId(String email) throws Exception {
        String birth = LocalDate.now().minusYears(30).toString();
        String register = """
                {
                  "email": "%s",
                  "password": "%s",
                  "confirmPassword": "%s",
                  "firstName": "Nome",
                  "lastName": "Sobrenome",
                  "birthDate": "%s"
                }
                """.formatted(email, PASSWORD_VALID, PASSWORD_VALID, birth);

        String response = mockMvc.perform(post(ApiPathConstants.ACCOUNTS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(register))
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
