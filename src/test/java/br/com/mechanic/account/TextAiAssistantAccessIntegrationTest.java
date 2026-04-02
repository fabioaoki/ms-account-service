package br.com.mechanic.account;

import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.AuthValidationConstants;
import br.com.mechanic.account.constant.TextAiAssistantRequestJsonConstants;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.security.integration-test-disable-jwt=false")
class TextAiAssistantAccessIntegrationTest {

    private static final String PASSWORD_VALID = "secret123";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST text-ai-assistant sem perfil autorizado retorna 403")
    void textAiEndpointWithoutAllowedProfileReturnsForbidden() throws Exception {
        String email = "no-profile-" + UUID.randomUUID() + "@email.com";
        long accountId = createAccountAndGetId(email);

        String body = """
                {
                  "%s": "T",
                  "%s": "Texto",
                  "%s": null,
                  "%s": true,
                  "%s": 30,
                  "%s": "Olá"
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
                        post(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.TEXT_AI_ASSISTANT_SEGMENT)
                                .with(JwtTestAuthentication.annotatorOnly(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(AuthValidationConstants.MESSAGE_ACCESS_DENIED));
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
}
