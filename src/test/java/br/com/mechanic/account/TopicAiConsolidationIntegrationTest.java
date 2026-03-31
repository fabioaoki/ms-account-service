package br.com.mechanic.account;

import br.com.mechanic.account.constant.AnnotatorLinkJsonConstants;
import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.TopicAiJsonConstants;
import br.com.mechanic.account.constant.TopicAiModelResponseJsonConstants;
import br.com.mechanic.account.constant.TopicCreateRequestJsonConstants;
import br.com.mechanic.account.constant.TopicPaginationConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import br.com.mechanic.account.service.openai.OpenAiChatCompletionPort;
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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({
        TopicAiConsolidationIntegrationTest.FixedClockConfiguration.class,
        TopicAiConsolidationIntegrationTest.StubOpenAiConfiguration.class
})
class TopicAiConsolidationIntegrationTest {

    private static final String PASSWORD_VALID = "secret123";

    private static final String FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY = "2026-06-16T12:35:00";

    /**
     * Resposta mínima aceita por {@link br.com.mechanic.account.service.topic.TopicAiConsolidationService#validateModelResponseJson}.
     */
    private static final String MINIMAL_VALID_OPENAI_JSON = """
            {"summary":"ok","context":null,"consolidationInsight":"ok","segments":[{"text":"b","contributions":[{"excerpt":"e","annotatorAccountIds":[1],"annotator_raw":[{"annotatorAccountId":1,"text":"e"}]}]}],"bibleReferences":[],"moderation":{"removedItems":[]}}
            """;

    @TestConfiguration
    static class FixedClockConfiguration {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-06-15T12:35:00Z"), ZoneOffset.UTC);
        }
    }

    @TestConfiguration
    static class StubOpenAiConfiguration {

        @Bean
        @Primary
        OpenAiChatCompletionPort stubOpenAiChatCompletionPort() {
            return (systemPrompt, userMessageJson) -> MINIMAL_VALID_OPENAI_JSON.trim();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST .../topics/{id}/ai-consolidation com tópico SPEAKER CLOSED retorna 400")
    void consolidateWhenSpeakerTopicClosedReturnsBadRequest() throws Exception {
        Long ownerId = createAccountAndSpeakerTopicSetup();
        String emailAnnotator = "ai-ant-" + UUID.randomUUID() + "@email.com";
        Long annotatorId =
                createAccountAndGetId(emailAnnotator, "Ana", "Nota", LocalDate.now().minusYears(30));

        long topicId = createSpeakerTopicViaApi(ownerId);

        mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkJsonAnnotatorOnly(annotatorId))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        put(annotatorLinkResumePath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkResumeJson(annotatorId, "Nota antes de fechar."))
                )
                .andExpect(status().isOk());

        mockMvc.perform(patch(accountTopicClosePath(ownerId, topicId)))
                .andExpect(status().isOk());

        mockMvc.perform(
                        post(topicAiConsolidationPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(TopicValidationConstants.MESSAGE_TOPIC_MUST_BE_OPEN_FOR_AI_CONSOLIDATION)
                );
    }

    @Test
    @DisplayName("POST .../ai-consolidation com tópico OPEN, vínculo e resumo grava relatório e define AI_REPORT_READY")
    void consolidateWhenOpenWithResumePersistsReportAndStatus() throws Exception {
        Long ownerId = createAccountAndSpeakerTopicSetup();
        String emailAnnotator = "ai-ant-" + UUID.randomUUID() + "@email.com";
        Long annotatorId =
                createAccountAndGetId(emailAnnotator, "Ana", "Nota", LocalDate.now().minusYears(30));

        long topicId = createSpeakerTopicViaApi(ownerId);

        mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkJsonAnnotatorOnly(annotatorId))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        put(annotatorLinkResumePath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkResumeJson(annotatorId, "Nota literal do anotador."))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        post(topicAiConsolidationPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.%s".formatted(TopicAiJsonConstants.TOPIC_ID)).value(topicId))
                .andExpect(jsonPath("$.%s".formatted(TopicAiJsonConstants.TOPIC_OWNER_ACCOUNT_ID)).value(ownerId))
                .andExpect(
                        jsonPath(
                                "$.%s.%s".formatted(
                                        TopicAiJsonConstants.RESPONSE_PAYLOAD,
                                        TopicAiModelResponseJsonConstants.SUMMARY
                                )
                        ).value("ok")
                );

        mockMvc.perform(get(accountTopicItemPath(ownerId, topicId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(TopicStatusEnum.AI_REPORT_READY.name()));

        mockMvc.perform(get(topicAiReportsPath(ownerId, topicId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(
                        get(accountTopicAiReportsPath(ownerId))
                                .param("page", String.valueOf(TopicPaginationConstants.DEFAULT_PAGE_NUMBER))
                                .param("size", String.valueOf(TopicPaginationConstants.DEFAULT_PAGE_SIZE))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("POST .../ai-consolidation com perfil ANNOTATOR não altera status do tópico")
    void consolidateAnnotatorTopicDoesNotChangeTopicStatus() throws Exception {
        String emailOwner = "ai-own-ant-" + UUID.randomUUID() + "@email.com";
        Long ownerId = createAccountAndGetId(emailOwner, "Dono", "Anot", LocalDate.now().minusYears(26));

        String created = mockMvc.perform(
                        post(accountTopicsPath(ownerId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildTopicCreateJsonImplicitAnnotator("Tema annot IA", null))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long topicId = objectMapper.readTree(created).get("id").asLong();

        String emailOther = "ai-other-ant-" + UUID.randomUUID() + "@email.com";
        Long annotatorId =
                createAccountAndGetId(emailOther, "Outro", "User", LocalDate.now().minusYears(24));

        mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkJsonAnnotatorOnly(annotatorId))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        put(annotatorLinkResumePath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkResumeJson(annotatorId, "Resumo annot"))
                )
                .andExpect(status().isOk());

        mockMvc.perform(post(topicAiConsolidationPath(ownerId, topicId)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String topicJson = mockMvc.perform(get(accountTopicItemPath(ownerId, topicId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(topicJson);
        assertFalse(root.has("status"), "Tópico ANNOTATOR não expõe status após IA");
    }

    private Long createAccountAndSpeakerTopicSetup() throws Exception {
        String email = "ai-spk-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        return accountId;
    }

    private void linkProfileForTopicCreation(Long accountId, AccountProfileTypeEnum profileType) throws Exception {
        mockMvc.perform(
                        post(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(profileType))
                )
                .andExpect(status().isCreated());
    }

    private Long createAccountAndGetId(String email, String firstName, String lastName, LocalDate birthDate)
            throws Exception {
        String body = buildJson(email, PASSWORD_VALID, PASSWORD_VALID, firstName, lastName, birthDate.toString());

        String response = mockMvc
                .perform(
                        post(ApiPathConstants.ACCOUNTS_BASE_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
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
                """
                .formatted(email, password, confirmPassword, firstName, lastName, birthDate);
    }

    private long createSpeakerTopicViaApi(Long accountId) throws Exception {
        String bodyCreate = buildTopicJson(
                "Topico consolidacao IA",
                null,
                AccountProfileTypeEnum.SPEAKER,
                endDateJsonSuffix(FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY)
        );
        String created = mockMvc
                .perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyCreate)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(created).get("id").asLong();
    }

    private static String topicAiConsolidationPath(Long accountId, long topicId) {
        return accountTopicItemPath(accountId, topicId) + ApiPathConstants.TOPIC_AI_CONSOLIDATION_SEGMENT;
    }

    private static String topicAiReportsPath(Long accountId, long topicId) {
        return accountTopicItemPath(accountId, topicId) + ApiPathConstants.TOPIC_AI_REPORTS_SEGMENT;
    }

    private static String accountTopicAiReportsPath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.ACCOUNT_TOPIC_AI_REPORTS_SEGMENT;
    }

    private static String accountTopicsPath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.TOPICS_SEGMENT;
    }

    private static String accountTopicItemPath(Long accountId, long topicId) {
        return accountTopicsPath(accountId) + "/" + topicId;
    }

    private static String accountTopicClosePath(Long accountId, long topicId) {
        return accountTopicItemPath(accountId, topicId) + ApiPathConstants.TOPIC_CLOSE_SEGMENT;
    }

    private static String annotatorLinkPath(Long topicOwnerAccountId, long topicId) {
        return accountTopicItemPath(topicOwnerAccountId, topicId) + ApiPathConstants.ANNOTATOR_LINK_SEGMENT;
    }

    private static String annotatorLinkResumePath(Long topicOwnerAccountId, long topicId) {
        return annotatorLinkPath(topicOwnerAccountId, topicId) + ApiPathConstants.ANNOTATOR_LINK_RESUME_SEGMENT;
    }

    private static String accountProfilesPath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.ACCOUNT_PROFILES_SEGMENT;
    }

    private static String buildLinkProfileJson(AccountProfileTypeEnum profileType) {
        return """
                {"profileType": "%s"}
                """.formatted(profileType.name());
    }

    private static String buildAnnotatorLinkJsonAnnotatorOnly(Long annotatorAccountId) {
        return """
                {"%s": %d}
                """
                .formatted(AnnotatorLinkJsonConstants.ANNOTATOR_ACCOUNT_ID, annotatorAccountId);
    }

    private static String buildAnnotatorLinkResumeJson(Long annotatorAccountId, String resume) {
        return """
                {
                  "%s": %d,
                  "%s": "%s"
                }
                """
                .formatted(
                        AnnotatorLinkJsonConstants.ANNOTATOR_ACCOUNT_ID,
                        annotatorAccountId,
                        AnnotatorLinkJsonConstants.RESUME,
                        resume.replace("\\", "\\\\").replace("\"", "\\\"")
                );
    }

    private static String endDateJsonSuffix(String endDateIsoLocal) {
        return ", \"" + TopicCreateRequestJsonConstants.END_DATE + "\": \"" + endDateIsoLocal + "\"";
    }

    private static String buildTopicCreateJsonImplicitAnnotator(String tema, String contexto) {
        if (contexto == null) {
            return """
                    {"%s": "%s"}
                    """
                    .formatted(TopicCreateRequestJsonConstants.TITLE, escapeJson(tema));
        }
        return """
                {
                  "%s": "%s",
                  "%s": "%s"
                }
                """
                .formatted(
                        TopicCreateRequestJsonConstants.TITLE,
                        escapeJson(tema),
                        TopicCreateRequestJsonConstants.CONTEXT,
                        escapeJson(contexto)
                );
    }

    private static String buildTopicJson(
            String tema,
            String contexto,
            AccountProfileTypeEnum profileType,
            String extraCommaPrefixedJson
    ) {
        String extra = extraCommaPrefixedJson == null ? "" : extraCommaPrefixedJson;
        if (contexto == null) {
            return """
                    {
                      "%s": "%s",
                      "%s": "%s"%s
                    }
                    """
                    .formatted(
                            TopicCreateRequestJsonConstants.TITLE,
                            escapeJson(tema),
                            TopicCreateRequestJsonConstants.PROFILE_TYPE,
                            profileType.name(),
                            extra
                    );
        }
        return """
                {
                  "%s": "%s",
                  "%s": "%s",
                  "%s": "%s"%s
                }
                """
                .formatted(
                        TopicCreateRequestJsonConstants.TITLE,
                        escapeJson(tema),
                        TopicCreateRequestJsonConstants.CONTEXT,
                        escapeJson(contexto),
                        TopicCreateRequestJsonConstants.PROFILE_TYPE,
                        profileType.name(),
                        extra
                );
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
