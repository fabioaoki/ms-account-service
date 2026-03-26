package br.com.mechanic.account;

import br.com.mechanic.account.constant.AccountRegistrationValidationConstants;
import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.AuthValidationConstants;
import br.com.mechanic.account.constant.AccountUpdateValidationConstants;
import br.com.mechanic.account.constant.ExceptionMessageConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerIntegrationTest {

    private static final String PASSWORD_VALID = "secret123";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/v1/accounts retorna 201 com perfil ANNOTATOR e nome formatado")
    void createAccountReturnsCreatedWithAnnotatorAndFormattedName() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@email.com";
        String birthDate = LocalDate.now().minusYears(25).toString();
        String body = buildJson(email, PASSWORD_VALID, PASSWORD_VALID, "fabio", "de carvalho", birthDate);

        mockMvc.perform(post(ApiPathConstants.ACCOUNTS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email.toLowerCase()))
                .andExpect(jsonPath("$.name").value("Fabio de Carvalho"))
                .andExpect(jsonPath("$.profileType").value("ANNOTATOR"))
                .andExpect(jsonPath("$.birthDate").value(birthDate))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("POST /api/v1/accounts aceita dominio .com.br")
    void createAccountAcceptsComBrDomain() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@servico.com.br";
        String birthDate = LocalDate.now().minusYears(30).toString();
        String body = buildJson(email, PASSWORD_VALID, PASSWORD_VALID, "Ana", "Silva", birthDate);

        mockMvc.perform(post(ApiPathConstants.ACCOUNTS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email.toLowerCase()))
                .andExpect(jsonPath("$.profileType").value("ANNOTATOR"));
    }

    @Test
    @DisplayName("POST /api/v1/accounts com e-mail ja cadastrado retorna 400 com mensagem generica")
    void createAccountDuplicateEmailReturnsGenericBadRequest() throws Exception {
        String email = "dup-" + UUID.randomUUID() + "@email.com";
        String birthDate = LocalDate.now().minusYears(20).toString();
        String body = buildJson(email, PASSWORD_VALID, PASSWORD_VALID, "Nome", "Sobrenome", birthDate);

        mockMvc.perform(post(ApiPathConstants.ACCOUNTS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post(ApiPathConstants.ACCOUNTS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ExceptionMessageConstants.GENERIC_REGISTRATION_FAILURE));
    }

    @Test
    @DisplayName("POST /api/v1/accounts com idade inferior a 12 anos retorna 400")
    void createAccountUnderMinAgeReturnsBadRequest() throws Exception {
        String email = "young-" + UUID.randomUUID() + "@email.com";
        String birthDate = LocalDate.now().minusYears(10).toString();
        String body = buildJson(email, PASSWORD_VALID, PASSWORD_VALID, "Crianca", "Teste", birthDate);

        mockMvc.perform(post(ApiPathConstants.ACCOUNTS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountRegistrationValidationConstants.MESSAGE_MIN_AGE_NOT_MET));
    }

    @Test
    @DisplayName("POST /api/v1/accounts com idade superior a 90 anos retorna 400")
    void createAccountOverMaxAgeReturnsBadRequest() throws Exception {
        String email = "old-" + UUID.randomUUID() + "@email.com";
        String birthDate = LocalDate.now().minusYears(95).toString();
        String body = buildJson(email, PASSWORD_VALID, PASSWORD_VALID, "Idoso", "Teste", birthDate);

        mockMvc.perform(post(ApiPathConstants.ACCOUNTS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountRegistrationValidationConstants.MESSAGE_MAX_AGE_EXCEEDED));
    }

    @Test
    @DisplayName("POST /api/v1/accounts com senhas diferentes retorna 400")
    void createAccountPasswordMismatchReturnsBadRequest() throws Exception {
        String email = "pwd-" + UUID.randomUUID() + "@email.com";
        String birthDate = LocalDate.now().minusYears(22).toString();
        String body = buildJson(email, PASSWORD_VALID, "outrasenha", "Nome", "Sobrenome", birthDate);

        mockMvc.perform(post(ApiPathConstants.ACCOUNTS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(AccountRegistrationValidationConstants.MESSAGE_PASSWORD_AND_CONFIRMATION_MUST_MATCH));
    }

    @Test
    @DisplayName("POST /api/v1/accounts com e-mail fora de .com ou .com.br retorna 400")
    void createAccountInvalidEmailDomainReturnsBadRequest() throws Exception {
        String email = "bad-" + UUID.randomUUID() + "@empresa.org";
        String birthDate = LocalDate.now().minusYears(22).toString();
        String body = buildJson(email, PASSWORD_VALID, PASSWORD_VALID, "Nome", "Sobrenome", birthDate);

        mockMvc.perform(post(ApiPathConstants.ACCOUNTS_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("email: " + AuthValidationConstants.MESSAGE_LOGIN_EMAIL_DOMAIN));
    }

    private static String buildJson(
            String email,
            String password,
            String confirmPassword,
            String firstName,
            String lastName,
            String birthDate
    ) {
        return """
                {
                  "email": "%s",
                  "password": "%s",
                  "confirmPassword": "%s",
                  "firstName": "%s",
                  "lastName": "%s",
                  "birthDate": "%s"
                }
                """.formatted(email, password, confirmPassword, firstName, lastName, birthDate);
    }

    @Test
    @DisplayName("PATCH /api/v1/accounts/{accountId} atualiza os 3 campos com sucesso")
    void updateAccountWithAllFieldsReturnsOk() throws Exception {
        String email = "upd-" + UUID.randomUUID() + "@email.com";
        LocalDate initialBirthDate = LocalDate.now().minusYears(25);
        Long accountId = createAccountAndGetId(email, "fabio", "de carvalho", initialBirthDate);

        LocalDate newBirthDate = LocalDate.now().minusYears(30);
        String body = buildUpdateJson("ana", "silva", newBirthDate);

        mockMvc.perform(
                        patch(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Silva"))
                .andExpect(jsonPath("$.birthDate").value(newBirthDate.toString()))
                .andExpect(jsonPath("$.lastUpdatedAt").exists());
    }

    @Test
    @DisplayName("PATCH /api/v1/accounts/{accountId} permite atualizar apenas o firstName")
    void updateAccountWithOnlyFirstNameReturnsOk() throws Exception {
        String email = "upd-" + UUID.randomUUID() + "@email.com";
        LocalDate initialBirthDate = LocalDate.now().minusYears(25);
        Long accountId = createAccountAndGetId(email, "fabio", "de carvalho", initialBirthDate);

        LocalDate newBirthDate = LocalDate.now().minusYears(30);
        String body = buildUpdateJsonOnlyFirstName("ana", newBirthDate);

        mockMvc.perform(
                        patch(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana de Carvalho"))
                .andExpect(jsonPath("$.birthDate").value(newBirthDate.toString()))
                .andExpect(jsonPath("$.lastUpdatedAt").exists());
    }

    @Test
    @DisplayName("PATCH /api/v1/accounts/{accountId} permite atualizar apenas o lastName")
    void updateAccountWithOnlyLastNameReturnsOk() throws Exception {
        String email = "upd-" + UUID.randomUUID() + "@email.com";
        LocalDate initialBirthDate = LocalDate.now().minusYears(25);
        Long accountId = createAccountAndGetId(email, "fabio", "de carvalho", initialBirthDate);

        LocalDate newBirthDate = LocalDate.now().minusYears(30);
        String body = buildUpdateJsonOnlyLastName("silva", newBirthDate);

        mockMvc.perform(
                        patch(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fabio Silva"))
                .andExpect(jsonPath("$.birthDate").value(newBirthDate.toString()))
                .andExpect(jsonPath("$.lastUpdatedAt").exists());
    }

    @Test
    @DisplayName("PATCH /api/v1/accounts/{accountId} valida accountId inexistente")
    void updateAccountWithNonExistingAccountIdReturnsBadRequest() throws Exception {
        Long accountId = 999999L;
        LocalDate birthDate = LocalDate.now().minusYears(30);
        String body = buildUpdateJson("ana", "silva", birthDate);

        mockMvc.perform(
                        patch(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
    }

    @Test
    @DisplayName("PATCH /api/v1/accounts/{accountId} firstName vazio retorna 400")
    void updateAccountWithEmptyFirstNameReturnsBadRequest() throws Exception {
        String email = "upd-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "fabio", "de carvalho", LocalDate.now().minusYears(25));
        LocalDate birthDate = LocalDate.now().minusYears(30);

        String body = buildUpdateJson("", "silva", birthDate);

        mockMvc.perform(
                        patch(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountUpdateValidationConstants.MESSAGE_FIRST_NAME_REQUIRED));
    }

    @Test
    @DisplayName("PATCH /api/v1/accounts/{accountId} sigla no nome retorna 400")
    void updateAccountWithSiglaNameReturnsBadRequest() throws Exception {
        String email = "upd-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "fabio", "de carvalho", LocalDate.now().minusYears(25));
        LocalDate birthDate = LocalDate.now().minusYears(30);

        String body = buildUpdateJson("ABC", "silva", birthDate);

        mockMvc.perform(
                        patch(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountUpdateValidationConstants.MESSAGE_NAME_CANNOT_BE_SIGLA));
    }

    @Test
    @DisplayName("PATCH /api/v1/accounts/{accountId} nome com caracteres invalidos retorna 400")
    void updateAccountWithInvalidNameTypeReturnsBadRequest() throws Exception {
        String email = "upd-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "fabio", "de carvalho", LocalDate.now().minusYears(25));
        LocalDate birthDate = LocalDate.now().minusYears(30);

        String body = buildUpdateJson("Ana1", "silva", birthDate);

        mockMvc.perform(
                        patch(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountUpdateValidationConstants.MESSAGE_NAME_INVALID_FORMAT));
    }

    @Test
    @DisplayName("PATCH /api/v1/accounts/{accountId} idade abaixo do minimo retorna 400")
    void updateAccountWithBirthDateUnderMinAgeReturnsBadRequest() throws Exception {
        String email = "upd-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "fabio", "de carvalho", LocalDate.now().minusYears(25));
        LocalDate birthDate = LocalDate.now().minusYears(10);

        String body = buildUpdateJson("ana", "silva", birthDate);

        mockMvc.perform(
                        patch(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountRegistrationValidationConstants.MESSAGE_MIN_AGE_NOT_MET));
    }

    @Test
    @DisplayName("PATCH /api/v1/accounts/{accountId} idade acima do maximo retorna 400")
    void updateAccountWithBirthDateOverMaxAgeReturnsBadRequest() throws Exception {
        String email = "upd-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "fabio", "de carvalho", LocalDate.now().minusYears(25));
        LocalDate birthDate = LocalDate.now().minusYears(95);

        String body = buildUpdateJson("ana", "silva", birthDate);

        mockMvc.perform(
                        patch(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountRegistrationValidationConstants.MESSAGE_MAX_AGE_EXCEEDED));
    }

    private Long createAccountAndGetId(String email, String firstName, String lastName, LocalDate birthDate) throws Exception {
        String body = buildJson(email, PASSWORD_VALID, PASSWORD_VALID, firstName, lastName, birthDate.toString());

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

    private static String buildUpdateJson(String firstName, String lastName, LocalDate birthDate) {
        return """
                {
                  "firstName": "%s",
                  "lastName": "%s",
                  "birthDate": "%s"
                }
                """.formatted(firstName, lastName, birthDate.toString());
    }

    private static String buildUpdateJsonOnlyFirstName(String firstName, LocalDate birthDate) {
        return """
                {
                  "firstName": "%s",
                  "birthDate": "%s"
                }
                """.formatted(firstName, birthDate.toString());
    }

    private static String buildUpdateJsonOnlyLastName(String lastName, LocalDate birthDate) {
        return """
                {
                  "lastName": "%s",
                  "birthDate": "%s"
                }
                """.formatted(lastName, birthDate.toString());
    }
}
