package br.com.mechanic.account;

import br.com.mechanic.account.constant.AccountUpdateValidationConstants;
import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import br.com.mechanic.account.repository.account.jpa.TopicRepositoryJpa;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TopicControllerIntegrationTest {

    private static final String PASSWORD_VALID = "secret123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TopicRepositoryJpa topicRepositoryJpa;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST .../topics com conta ACTIVE e perfil complementar (nao ANNOTATOR) retorna 201 e status OPEN")
    void createTopicWithActiveAccountReturnsCreated() throws Exception {
        String email = "topic-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);

        String body = buildTopicJson("Tema valido para o topico", null, AccountProfileTypeEnum.SPEAKER);

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.tema").value("Tema valido para o topico"))
                .andExpect(jsonPath("$.contexto").doesNotExist())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.lastUpdatedAt").exists())
                .andExpect(jsonPath("$.status").value(TopicStatusEnum.OPEN.name()))
                .andExpect(jsonPath("$.profile_type").value(AccountProfileTypeEnum.SPEAKER.name()));

        assertEquals(1L, topicRepositoryJpa.countByAccount_Id(accountId));
    }

    @Test
    @DisplayName("POST .../topics com contexto opcional persiste e retorna contexto")
    void createTopicWithOptionalContextReturnsContexto() throws Exception {
        String email = "topic-ctx-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "ana", "silva", LocalDate.now().minusYears(28));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.COACH);

        String body = buildTopicJson(
                "Outro tema ok",
                "Contexto detalhado do topico",
                AccountProfileTypeEnum.COACH
        );

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contexto").value("Contexto detalhado do topico"));
    }

    @Test
    @DisplayName("POST .../topics com conta INACTIVE retorna 400")
    void createTopicWithInactiveAccountReturnsBadRequest() throws Exception {
        String email = "topic-inact-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(22));

        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);

        mockMvc.perform(patch(accountDeactivatePath(accountId)))
                .andExpect(status().isOk());

        String body = buildTopicJson("Tema qualquer", null, AccountProfileTypeEnum.SPEAKER);

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_TO_CREATE_TOPIC));

        assertEquals(0L, topicRepositoryJpa.countByAccount_Id(accountId));
    }

    @Test
    @DisplayName("POST .../topics com tema de uma letra retorna 400")
    void createTopicWithSingleLetterTemaReturnsBadRequest() throws Exception {
        String email = "topic-1c-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(24));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);

        String body = buildTopicJson("x", null, AccountProfileTypeEnum.SPEAKER);

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_TEMA_INVALID_LENGTH));
    }

    @Test
    @DisplayName("POST .../topics com profile_type nao vinculado a conta retorna 400")
    void createTopicWithUnlinkedProfileTypeReturnsBadRequest() throws Exception {
        String email = "topic-prf-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(23));

        String body = buildTopicJson("Tema com perfil inexistente na conta", null, AccountProfileTypeEnum.SPEAKER);

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_PROFILE_TYPE_NOT_LINKED_TO_ACCOUNT));
    }

    @Test
    @DisplayName("POST .../topics com conta inexistente retorna 400")
    void createTopicWithUnknownAccountReturnsBadRequest() throws Exception {
        String body = buildTopicJson("Tema ok", null, AccountProfileTypeEnum.SPEAKER);

        mockMvc.perform(
                        post(accountTopicsPath(999999L))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
    }

    @Test
    @DisplayName("POST .../topics apos vincular SPEAKER aceita topico com SPEAKER")
    void createTopicWithLinkedSpeakerProfileReturnsCreated() throws Exception {
        String email = "topic-spk-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "joao", "santos", LocalDate.now().minusYears(30));

        mockMvc.perform(
                        post(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.SPEAKER))
                )
                .andExpect(status().isCreated());

        String body = buildTopicJson("Palestra principal", "Resumo", AccountProfileTypeEnum.SPEAKER);

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profile_type").value(AccountProfileTypeEnum.SPEAKER.name()));
    }

    @Test
    @DisplayName("POST .../topics com profile_type ANNOTATOR retorna 400")
    void createTopicWithAnnotatorProfileReturnsBadRequest() throws Exception {
        String email = "topic-ant-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));

        String body = buildTopicJson("Tema valido", null, AccountProfileTypeEnum.ANNOTATOR);

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_ANNOTATOR_CANNOT_CREATE_TOPIC));

        assertEquals(0L, topicRepositoryJpa.countByAccount_Id(accountId));
    }

    @Test
    @DisplayName("POST .../topics com tema apenas espacos retorna 400 (validacao)")
    void createTopicWithBlankTemaReturnsBadRequest() throws Exception {
        String email = "topic-blank-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(26));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);

        String body = buildTopicJson("   ", null, AccountProfileTypeEnum.SPEAKER);

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message").value("tema: " + TopicValidationConstants.MESSAGE_TEMA_REQUIRED)
                );
    }

    @Test
    @DisplayName("POST .../topics normaliza tema com espacos nas extremidades")
    void createTopicTrimsTemaEdges() throws Exception {
        String email = "topic-trim-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(27));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.BISHOP);

        String body = buildTopicJson("  Ab  ", null, AccountProfileTypeEnum.BISHOP);

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tema").value("Ab"));
    }

    private void linkProfileForTopicCreation(Long accountId, AccountProfileTypeEnum profileType) throws Exception {
        mockMvc.perform(
                        post(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(profileType))
                )
                .andExpect(status().isCreated());
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

    private static String accountTopicsPath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.TOPICS_SEGMENT;
    }

    private static String accountProfilesPath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.ACCOUNT_PROFILES_SEGMENT;
    }

    private static String accountDeactivatePath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.ACCOUNT_DEACTIVATE_SEGMENT;
    }

    private static String buildLinkProfileJson(AccountProfileTypeEnum profileType) {
        return """
                {"profileType": "%s"}
                """.formatted(profileType.name());
    }

    private static String buildTopicJson(String tema, String contexto, AccountProfileTypeEnum profileType) {
        if (contexto == null) {
            return """
                    {
                      "tema": "%s",
                      "profile_type": "%s"
                    }
                    """.formatted(escapeJson(tema), profileType.name());
        }
        return """
                {
                  "tema": "%s",
                  "contexto": "%s",
                  "profile_type": "%s"
                }
                """.formatted(escapeJson(tema), escapeJson(contexto), profileType.name());
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
