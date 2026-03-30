package br.com.mechanic.account;

import br.com.mechanic.account.constant.AnnotatorLinkJsonConstants;
import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.TopicAnnotatorLinkValidationConstants;
import br.com.mechanic.account.constant.TopicCreateRequestJsonConstants;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.repository.account.AccountProfileRepository;
import br.com.mechanic.account.repository.account.jpa.TopicAnnotatorLinkHistoryRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AnnotatorControllerIntegrationTest.FixedClockConfiguration.class)
class AnnotatorControllerIntegrationTest {

    private static final String PASSWORD_VALID = "secret123";

    private static final String FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY = "2026-06-16T12:35:00";

    @TestConfiguration
    static class FixedClockConfiguration {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-06-15T12:35:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TopicAnnotatorLinkHistoryRepository topicAnnotatorLinkHistoryRepository;

    @Autowired
    private AccountProfileRepository accountProfileRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST .../annotator-link sem resume retorna 201 e grava histórico (resume omitido no JSON)")
    void createAnnotatorLinkReturnsCreatedAndPersistsHistory() throws Exception {
        Long ownerId = newActiveSpeakerOwner();
        Long annotatorId = newActiveAnnotatorOnly();
        long topicId = createSpeakerTopicAndGetId(ownerId);

        String body = buildAnnotatorLinkJsonAnnotatorOnly(annotatorId);

        String response = mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$." + AnnotatorLinkJsonConstants.ID).exists())
                .andExpect(jsonPath("$." + AnnotatorLinkJsonConstants.TOPIC_ID).value(topicId))
                .andExpect(jsonPath("$." + AnnotatorLinkJsonConstants.TOPIC_OWNER_ACCOUNT_ID).value(ownerId))
                .andExpect(jsonPath("$." + AnnotatorLinkJsonConstants.ANNOTATOR_ACCOUNT_ID).value(annotatorId))
                .andExpect(jsonPath("$." + AnnotatorLinkJsonConstants.RESUME).doesNotExist())
                .andExpect(jsonPath("$." + AnnotatorLinkJsonConstants.CREATED_AT).exists())
                .andExpect(jsonPath("$." + AnnotatorLinkJsonConstants.LAST_UPDATED_AT).doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long linkId = objectMapper.readTree(response).get(AnnotatorLinkJsonConstants.ID).asLong();
        assertEquals(1L, topicAnnotatorLinkHistoryRepository.countByLink_Id(linkId));
    }

    @Test
    @DisplayName("POST .../annotator-link com tópico SPEAKER em CLOSED retorna 400")
    void createAnnotatorLinkWhenSpeakerTopicClosedReturnsBadRequest() throws Exception {
        Long ownerId = newActiveSpeakerOwner();
        Long annotatorId = newActiveAnnotatorOnly();
        long topicId = createSpeakerTopicAndGetId(ownerId);

        mockMvc.perform(patch(accountTopicsPath(ownerId) + "/" + topicId + ApiPathConstants.TOPIC_CLOSE_SEGMENT))
                .andExpect(status().isOk());

        mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkJsonAnnotatorOnly(annotatorId))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        TopicAnnotatorLinkValidationConstants.MESSAGE_TOPIC_MUST_BE_OPEN_FOR_ANNOTATOR_LINK
                ));
    }

    @Test
    @DisplayName("POST .../annotator-link quando tópico não pertence ao accountId do path retorna 400")
    void createAnnotatorLinkWhenTopicNotOwnedReturnsBadRequest() throws Exception {
        Long ownerId = newActiveSpeakerOwner();
        Long otherId = newActiveSpeakerOwner();
        Long annotatorId = newActiveAnnotatorOnly();
        long topicId = createSpeakerTopicAndGetId(ownerId);

        mockMvc.perform(
                        post(annotatorLinkPath(otherId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkJsonAnnotatorOnly(annotatorId))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        TopicAnnotatorLinkValidationConstants.MESSAGE_TOPIC_NOT_FOUND_OR_NOT_OWNED_FOR_LINK
                ));
    }

    @Test
    @DisplayName("POST .../annotator-link com mesmo account do dono em tópico SPEAKER retorna 400")
    void createAnnotatorLinkSelfOnSpeakerTopicReturnsBadRequest() throws Exception {
        Long ownerId = newActiveSpeakerOwner();
        long topicId = createSpeakerTopicAndGetId(ownerId);

        mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkJsonAnnotatorOnly(ownerId))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        TopicAnnotatorLinkValidationConstants.MESSAGE_ANNOTATOR_MUST_DIFFER_FROM_TOPIC_OWNER_UNLESS_TOPIC_PROFILE_IS_ANNOTATOR
                ));
    }

    @Test
    @DisplayName("POST .../annotator-link com mesmo account do dono em tópico ANNOTATOR retorna 201")
    void createAnnotatorLinkSelfOnAnnotatorTopicReturnsCreated() throws Exception {
        Long ownerId = newActiveAnnotatorOwnerForImplicitTopic();
        long topicId = createAnnotatorTopicImplicitAndGetId(ownerId);

        mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkJsonAnnotatorOnly(ownerId))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$." + AnnotatorLinkJsonConstants.ANNOTATOR_ACCOUNT_ID).value(ownerId));
    }

    @Test
    @DisplayName("POST .../annotator-link mesmo anotador duas vezes no mesmo tópico retorna 400")
    void createAnnotatorLinkDuplicatePairReturnsBadRequest() throws Exception {
        Long ownerId = newActiveSpeakerOwner();
        Long annotatorId = newActiveAnnotatorOnly();
        long topicId = createSpeakerTopicAndGetId(ownerId);
        String body = buildAnnotatorLinkJsonAnnotatorOnly(annotatorId);

        mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkJsonAnnotatorOnly(annotatorId))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        TopicAnnotatorLinkValidationConstants.MESSAGE_TOPIC_ANNOTATOR_LINK_PAIR_ALREADY_EXISTS
                ));
    }

    @Test
    @DisplayName("POST .../annotator-link dois anotadores distintos no mesmo tópico retorna 201 em ambos")
    void createTwoDistinctAnnotatorLinksOnSameTopicReturnsCreatedTwice() throws Exception {
        Long ownerId = newActiveSpeakerOwner();
        Long annotatorIdA = newActiveAnnotatorOnly();
        Long annotatorIdB = newActiveAnnotatorOnly();
        long topicId = createSpeakerTopicAndGetId(ownerId);

        mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkJsonAnnotatorOnly(annotatorIdA))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$." + AnnotatorLinkJsonConstants.ANNOTATOR_ACCOUNT_ID).value(annotatorIdA));

        mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkJsonAnnotatorOnly(annotatorIdB))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$." + AnnotatorLinkJsonConstants.ANNOTATOR_ACCOUNT_ID).value(annotatorIdB));
    }

    @Test
    @DisplayName("POST .../annotator-link com resume opcional persiste e retorna texto")
    void createAnnotatorLinkWithResumeWhenProvidedReturnsResume() throws Exception {
        Long ownerId = newActiveSpeakerOwner();
        Long annotatorId = newActiveAnnotatorOnly();
        long topicId = createSpeakerTopicAndGetId(ownerId);

        mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkJson(annotatorId, "Resumo enviado opcionalmente."))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$." + AnnotatorLinkJsonConstants.RESUME).value("Resumo enviado opcionalmente."));
    }

    @Transactional
    @Test
    @DisplayName("POST .../annotator-link com conta anotadora sem perfil ANNOTATOR retorna 400")
    void createAnnotatorLinkWhenAnnotatorLacksProfileReturnsBadRequest() throws Exception {
        Long ownerId = newActiveSpeakerOwner();
        Long onlySpeakerId = newActiveSpeakerOwner();
        accountProfileRepository.deleteByAccount_IdAndProfile_ProfileType(
                onlySpeakerId,
                AccountProfileTypeEnum.ANNOTATOR
        );
        long topicId = createSpeakerTopicAndGetId(ownerId);

        mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkJsonAnnotatorOnly(onlySpeakerId))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        TopicAnnotatorLinkValidationConstants.MESSAGE_ANNOTATOR_MUST_HAVE_ANNOTATOR_PROFILE
                ));
    }

    @Test
    @DisplayName("POST .../annotator-link com dono INACTIVE retorna 400")
    void createAnnotatorLinkInactiveOwnerReturnsBadRequest() throws Exception {
        Long ownerId = newActiveSpeakerOwner();
        Long annotatorId = newActiveAnnotatorOnly();
        long topicId = createSpeakerTopicAndGetId(ownerId);

        mockMvc.perform(patch(accountDeactivatePath(ownerId)))
                .andExpect(status().isOk());

        mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkJsonAnnotatorOnly(annotatorId))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        TopicAnnotatorLinkValidationConstants.MESSAGE_ACCOUNTS_MUST_BE_ACTIVE_FOR_ANNOTATOR_LINK
                ));
    }

    @Test
    @DisplayName("POST .../annotator-link com anotador INACTIVE retorna 400")
    void createAnnotatorLinkInactiveAnnotatorReturnsBadRequest() throws Exception {
        Long ownerId = newActiveSpeakerOwner();
        Long annotatorId = newActiveAnnotatorOnly();
        long topicId = createSpeakerTopicAndGetId(ownerId);

        mockMvc.perform(patch(accountDeactivatePath(annotatorId)))
                .andExpect(status().isOk());

        mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkJsonAnnotatorOnly(annotatorId))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        TopicAnnotatorLinkValidationConstants.MESSAGE_ACCOUNTS_MUST_BE_ACTIVE_FOR_ANNOTATOR_LINK
                ));
    }

    private Long newActiveSpeakerOwner() throws Exception {
        String email = "ann-own-" + UUID.randomUUID() + "@email.com";
        Long id = createAccountAndGetId(email, "Dono", "Speaker", LocalDate.now().minusYears(28));
        linkProfileForTopicCreation(id, AccountProfileTypeEnum.SPEAKER);
        return id;
    }

    /**
     * Cadastro já associa {@link AccountProfileTypeEnum#ANNOTATOR}; não usar {@code POST /profiles} para ANNOTATOR.
     */
    private Long newActiveAnnotatorOnly() throws Exception {
        String email = "ann-ant-" + UUID.randomUUID() + "@email.com";
        return createAccountAndGetId(email, "Ana", "Notator", LocalDate.now().minusYears(30));
    }

    private Long newActiveAnnotatorOwnerForImplicitTopic() throws Exception {
        String email = "ann-own-implicit-" + UUID.randomUUID() + "@email.com";
        return createAccountAndGetId(email, "Dono", "Annot", LocalDate.now().minusYears(29));
    }

    private long createSpeakerTopicAndGetId(Long accountId) throws Exception {
        String body = """
                {
                  "%s": "Tema para link anotador",
                  "%s": "%s",
                  "%s": "%s"
                }
                """
                .formatted(
                        TopicCreateRequestJsonConstants.TITLE,
                        TopicCreateRequestJsonConstants.PROFILE_TYPE,
                        AccountProfileTypeEnum.SPEAKER.name(),
                        TopicCreateRequestJsonConstants.END_DATE,
                        FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY
                );
        String response = mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long createAnnotatorTopicImplicitAndGetId(Long accountId) throws Exception {
        String body = """
                {"%s": "Topico implícito annotator"}
                """.formatted(TopicCreateRequestJsonConstants.TITLE);
        String response = mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
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
        String body = """
                {
                  "email": "%s",
                  "password": "%s",
                  "confirmPassword": "%s",
                  "firstName": "%s",
                  "lastName": "%s",
                  "birthDate": "%s"
                }
                """.formatted(email, PASSWORD_VALID, PASSWORD_VALID, firstName, lastName, birthDate.toString());

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

    private static String buildAnnotatorLinkJson(Long annotatorAccountId, String resume) {
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

    private static String accountTopicsPath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.TOPICS_SEGMENT;
    }

    private static String annotatorLinkPath(Long accountId, long topicId) {
        return accountTopicsPath(accountId) + "/" + topicId + ApiPathConstants.ANNOTATOR_LINK_SEGMENT;
    }

    private static String accountProfilesPath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.ACCOUNT_PROFILES_SEGMENT;
    }

    private static String accountDeactivatePath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.ACCOUNT_DEACTIVATE_SEGMENT;
    }
}
