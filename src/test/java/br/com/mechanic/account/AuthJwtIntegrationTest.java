package br.com.mechanic.account;

import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.AuthJsonConstants;
import br.com.mechanic.account.constant.AuthJwtConstants;
import br.com.mechanic.account.constant.AuthTokenConstants;
import br.com.mechanic.account.constant.SecurityAuthorityConstants;
import br.com.mechanic.account.security.JwtTestAuthentication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.security.integration-test-disable-jwt=false")
class AuthJwtIntegrationTest {

    private static final String PASSWORD_VALID = "secret123";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/v1/auth/login credenciais inválidas retorna 401")
    void loginWithInvalidPasswordReturnsUnauthorized() throws Exception {
        String body = """
                {"email":"nope@email.com","password":"wrongpassw0rd"}
                """;

        mockMvc.perform(
                        post(ApiPathConstants.AUTH_BASE_PATH + ApiPathConstants.AUTH_LOGIN_SEGMENT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login retorna access, refresh e authorities")
    void loginReturnsTokenAndAuthorities() throws Exception {
        String email = "jwt-" + UUID.randomUUID() + "@email.com";
        createAccountAndGetId(email);

        String body = """
                {"email":"%s","password":"%s"}
                """.formatted(email, PASSWORD_VALID);

        mockMvc.perform(
                        post(ApiPathConstants.AUTH_BASE_PATH + ApiPathConstants.AUTH_LOGIN_SEGMENT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + AuthJsonConstants.ACCESS_TOKEN).isString())
                .andExpect(jsonPath("$." + AuthJsonConstants.REFRESH_TOKEN).isString())
                .andExpect(jsonPath("$." + AuthJsonConstants.TOKEN_TYPE).value(AuthTokenConstants.BEARER_TOKEN_TYPE))
                .andExpect(jsonPath("$." + AuthJsonConstants.EXPIRES_IN_SECONDS)
                        .value((int) AuthJwtConstants.DEFAULT_ACCESS_TOKEN_EXPIRATION_SECONDS))
                .andExpect(jsonPath("$." + AuthJsonConstants.REFRESH_EXPIRES_IN_SECONDS)
                        .value((int) AuthJwtConstants.DEFAULT_REFRESH_TOKEN_EXPIRATION_SECONDS))
                .andExpect(jsonPath("$." + AuthJsonConstants.AUTHORITIES).isArray())
                .andExpect(jsonPath("$." + AuthJsonConstants.AUTHORITIES, hasItem(SecurityAuthorityConstants.ANNOTATOR)));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh com refresh válido retorna novo par de tokens")
    void refreshWithValidTokenReturnsNewTokens() throws Exception {
        String email = "jwt-refresh-" + UUID.randomUUID() + "@email.com";
        createAccountAndGetId(email);
        String loginBody = "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD_VALID);
        String loginResponse = mockMvc.perform(
                        post(ApiPathConstants.AUTH_BASE_PATH + ApiPathConstants.AUTH_LOGIN_SEGMENT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String refresh = objectMapper.readTree(loginResponse).get(AuthJsonConstants.REFRESH_TOKEN).asText();
        ObjectNode refreshPayload = objectMapper.createObjectNode();
        refreshPayload.put(AuthJsonConstants.REFRESH_TOKEN, refresh);
        String refreshRequestBody = objectMapper.writeValueAsString(refreshPayload);

        mockMvc.perform(
                        post(ApiPathConstants.AUTH_BASE_PATH + ApiPathConstants.AUTH_REFRESH_SEGMENT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshRequestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + AuthJsonConstants.ACCESS_TOKEN).isString())
                .andExpect(jsonPath("$." + AuthJsonConstants.REFRESH_TOKEN).isString())
                .andExpect(jsonPath("$." + AuthJsonConstants.EXPIRES_IN_SECONDS)
                        .value((int) AuthJwtConstants.DEFAULT_ACCESS_TOKEN_EXPIRATION_SECONDS));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} sem Authorization retorna 401")
    void getAccountWithoutTokenReturnsUnauthorized() throws Exception {
        String response = mockMvc.perform(post(ApiPathConstants.ACCOUNTS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRegisterJson("noauth-" + UUID.randomUUID() + "@email.com")))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} com JWT do próprio id retorna 200")
    void getAccountWithJwtReturnsOk() throws Exception {
        String email = "withjwt-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email);

        mockMvc.perform(
                        get(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId)
                                .with(JwtTestAuthentication.annotatorOnly(accountId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId));
    }

    private Long createAccountAndGetId(String email) throws Exception {
        String response = mockMvc.perform(post(ApiPathConstants.ACCOUNTS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRegisterJson(email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("id").asLong();
    }

    private static String buildRegisterJson(String email) {
        String birth = LocalDate.now().minusYears(25).toString();
        return """
                {
                  "email": "%s",
                  "password": "%s",
                  "confirmPassword": "%s",
                  "firstName": "Nome",
                  "lastName": "Sobrenome",
                  "birthDate": "%s"
                }
                """.formatted(email, PASSWORD_VALID, PASSWORD_VALID, birth);
    }
}
