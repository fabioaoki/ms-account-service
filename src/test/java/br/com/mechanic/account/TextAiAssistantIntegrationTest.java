package br.com.mechanic.account;

import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.TextAiAssistantRequestJsonConstants;
import br.com.mechanic.account.constant.TextAiAssistantResponseJsonConstants;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.openai.assistant-text-ai-id=asst-integration-text-ai")
@Import(TextAiAssistantIntegrationTest.StubThreadTurnConfiguration.class)
class TextAiAssistantIntegrationTest {

    private static final String PASSWORD_VALID = "secret123";

    private static final String STUB_THREAD = "thread_integration_stub";

    private static final String AI_FIRST = """
            {"title":"Tema IA","allResume":"Texto consolidado pela IA","modificationResume":null,"modification":true,"chat":"Como posso ajudar?"}
            """;

    private static final String AI_SECOND = """
            {"title":"Tema IA","allResume":"Deve ser ignorado no DB","modificationResume":null,"modification":false,"chat":"Segunda resposta."}
            """;

    @TestConfiguration
    static class StubThreadTurnConfiguration {

        @Bean
        @Primary
        OpenAiAssistantThreadTurnPort stubOpenAiAssistantThreadTurnPort() {
            return (assistantId, existingOpenAiThreadId, userMessageJson) -> {
                if (existingOpenAiThreadId == null || existingOpenAiThreadId.isBlank()) {
                    return new OpenAiAssistantThreadTurnResult(STUB_THREAD, AI_FIRST.trim());
                }
                return new OpenAiAssistantThreadTurnResult(STUB_THREAD, AI_SECOND.trim());
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountTextAiSessionRepositoryJpa sessionRepositoryJpa;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST text-ai-assistant: primeira e segunda volta persistem resume conforme modification")
    void firstAndSecondTurnPersistResumeAccordingToModification() throws Exception {
        String email = "text-ai-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email);
        linkProfileForTextAiAccess(accountId);

        String firstBody = """
                {
                  "%s": "Minha palestra",
                  "%s": "Rascunho inicial do usuário.",
                  "%s": null,
                  "%s": true,
                  "%s": 60,
                  "%s": "Quero falar sobre fé."
                }
                """
                .formatted(
                        TextAiAssistantRequestJsonConstants.TITLE,
                        TextAiAssistantRequestJsonConstants.RESUME,
                        TextAiAssistantRequestJsonConstants.RESUME_MODIFICATION,
                        TextAiAssistantRequestJsonConstants.TIME,
                        TextAiAssistantRequestJsonConstants.EXPECTED,
                        TextAiAssistantRequestJsonConstants.CHAT
                );

        mockMvc.perform(
                        post(textAiPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(firstBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + TextAiAssistantResponseJsonConstants.THREAD_ID).value(STUB_THREAD))
                .andExpect(jsonPath("$." + TextAiAssistantResponseJsonConstants.MODIFICATION).value(true));

        assertEquals(
                1,
                sessionRepositoryJpa.findAll().stream()
                        .filter(s -> s.getAccount().getId().equals(accountId))
                        .count()
        );
        var sessionAfterFirst = sessionRepositoryJpa.findAll().stream()
                .filter(s -> s.getAccount().getId().equals(accountId))
                .findFirst()
                .orElseThrow();
        assertEquals("Texto consolidado pela IA", sessionAfterFirst.getResume());
        assertNull(sessionAfterFirst.getLastUpdatedAt());

        String secondBody = """
                {
                  "threadId": "%s",
                  "chat": "Sugestões?"
                }
                """
                .formatted(STUB_THREAD);

        mockMvc.perform(
                        post(textAiPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(secondBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + TextAiAssistantResponseJsonConstants.MODIFICATION).value(false));

        var sessionAfterSecond = sessionRepositoryJpa
                .findByAccount_IdAndOpenAiThreadId(accountId, STUB_THREAD)
                .orElseThrow();
        assertEquals("Texto consolidado pela IA", sessionAfterSecond.getResume());
        assertNotNull(sessionAfterSecond.getLastUpdatedAt());
    }

    /** Hoje {@code TextAiAssistantAccessConstants} inclui BISHOP; ajuste o perfil se expandir a lista. */
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
                """.formatted(email, PASSWORD_VALID, PASSWORD_VALID, LocalDate.now().minusYears(40));

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

    private static String textAiPath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.TEXT_AI_ASSISTANT_SEGMENT;
    }
}
