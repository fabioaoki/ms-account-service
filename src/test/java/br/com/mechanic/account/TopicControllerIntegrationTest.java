package br.com.mechanic.account;

import br.com.mechanic.account.constant.AccountUpdateValidationConstants;
import br.com.mechanic.account.constant.AnnotatorLinkJsonConstants;
import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.TopicCreateRequestJsonConstants;
import br.com.mechanic.account.constant.TopicListQueryConstants;
import br.com.mechanic.account.constant.TopicCreationConstants;
import br.com.mechanic.account.constant.TopicPaginationConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.topic.Topic;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import br.com.mechanic.account.repository.account.jpa.AccountRepositoryJpa;
import br.com.mechanic.account.repository.account.jpa.TopicHistoryRepository;
import br.com.mechanic.account.repository.account.jpa.TopicRepositoryJpa;
import br.com.mechanic.account.service.topic.TopicService;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TopicControllerIntegrationTest.FixedClockConfiguration.class)
class TopicControllerIntegrationTest {

    private static final String PASSWORD_VALID = "secret123";

    /**
     * {@link TopicService} uses {@link LocalDateTime#now(Clock)}; fixed instant so {@code end_date} tests are stable.
     * Creation time = 2026-06-15T12:35:00 UTC.
     */
    private static final String FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY = "2026-06-16T12:35:00";

    private static final String FIXED_NOW_END_DATE_VALID_AT_MAX = "2026-06-18T12:35:00";

    private static final String FIXED_NOW_END_DATE_INVALID_AFTER_MAX = "2026-06-18T12:35:01";

    private static final String FIXED_NOW_END_DATE_INVALID_BEFORE = "2026-06-14T12:35:00";

    /**
     * Um dia antes de {@link #FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY} / relógio fixo, para validar {@code end_date}
     * em relação a {@link Topic#getCreatedAt()}, não ao instante atual.
     */
    private static final String TOPIC_ANCHOR_ONE_DAY_BEFORE_FIXED_NOW = "2026-06-14T12:35:00";

    private static final String END_DATE_ON_MAX_RELATIVE_TO_TOPIC_ANCHOR = "2026-06-17T12:35:00";

    private static final String END_DATE_INVALID_AFTER_TOPIC_ANCHOR_PLUS_THREE_DAYS = "2026-06-17T12:35:01";

    /**
     * Alinhado ao {@link Clock} fixo do teste; usado em inserts diretos de {@link Topic} SPEAKER no repositorio.
     */
    private static final String SPEAKER_TOPIC_DB_DEFAULT_CREATED_AT = "2026-06-15T12:35:00";

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
    private TopicRepositoryJpa topicRepositoryJpa;

    @Autowired
    private AccountRepositoryJpa accountRepositoryJpa;

    @Autowired
    private TopicHistoryRepository topicHistoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST .../topics com conta ACTIVE e SPEAKER vinculado em account_profile retorna 201")
    void createTopicWithActiveAccountReturnsCreated() throws Exception {
        String email = "topic-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);

        String body = buildTopicJson(
                "Tema valido para o topico",
                null,
                AccountProfileTypeEnum.SPEAKER,
                endDateJsonSuffix(FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY)
        );

        String response = mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$." + TopicCreateRequestJsonConstants.ACCOUNT_NAME).value("Nome Sobrenome"))
                .andExpect(jsonPath("$.title").value("Tema valido para o topico"))
                .andExpect(jsonPath("$.context").doesNotExist())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.lastUpdatedAt").doesNotExist())
                .andExpect(jsonPath("$.status").value(TopicStatusEnum.OPEN.name()))
                .andExpect(jsonPath("$.end_date").value(FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY))
                .andExpect(jsonPath("$.profile_type").value(AccountProfileTypeEnum.SPEAKER.name()))
                .andExpect(jsonPath("$." + TopicCreateRequestJsonConstants.TOPIC_ANNOTATOR_LINKS).isArray())
                .andExpect(jsonPath("$." + TopicCreateRequestJsonConstants.TOPIC_ANNOTATOR_LINKS + ".length()").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long topicId = objectMapper.readTree(response).get("id").asLong();
        assertEquals(1L, topicHistoryRepository.countByTopic_Id(topicId));
        assertEquals(TopicStatusEnum.OPEN, topicHistoryRepository.findByTopic_IdOrderByIdAsc(topicId).get(0).getStatus());
        assertNotNull(topicHistoryRepository.findByTopic_IdOrderByIdAsc(topicId).get(0).getCreatedAt());

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
                AccountProfileTypeEnum.COACH,
                endDateJsonSuffix(FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY)
        );

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.context").value("Contexto detalhado do topico"))
                .andExpect(jsonPath("$.end_date").value(FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY))
                .andExpect(jsonPath("$.status").value(TopicStatusEnum.OPEN.name()));
    }

    @Test
    @DisplayName("POST .../topics com conta INACTIVE retorna 400")
    void createTopicWithInactiveAccountReturnsBadRequest() throws Exception {
        String email = "topic-inact-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(22));

        mockMvc.perform(patch(accountDeactivatePath(accountId)))
                .andExpect(status().isOk());

        String body = buildTopicJson(
                "Tema qualquer",
                null,
                AccountProfileTypeEnum.SPEAKER,
                endDateJsonSuffix(FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY)
        );

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS));

        assertEquals(0L, topicRepositoryJpa.countByAccount_Id(accountId));
    }

    @Test
    @DisplayName("POST .../topics com tema de uma letra retorna 400")
    void createTopicWithSingleLetterTemaReturnsBadRequest() throws Exception {
        String email = "topic-1c-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(24));

        String body = buildTopicJson("x", null, AccountProfileTypeEnum.ANNOTATOR);

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_TOPIC_TITLE_INVALID_LENGTH));
    }

    @Test
    @DisplayName("POST .../topics com SPEAKER sem vinculo em account_profile retorna 400")
    void createTopicWithUnlinkedSpeakerProfileReturnsBadRequest() throws Exception {
        String email = "topic-prf-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(23));

        String body = buildTopicJson(
                "Tema valido com perfil nao vinculado",
                null,
                AccountProfileTypeEnum.SPEAKER,
                endDateJsonSuffix(FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY)
        );

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_PROFILE_TYPE_NOT_LINKED_TO_ACCOUNT));

        assertEquals(0L, topicRepositoryJpa.countByAccount_Id(accountId));
    }

    @Test
    @DisplayName("POST .../topics com conta inexistente retorna 400")
    void createTopicWithUnknownAccountReturnsBadRequest() throws Exception {
        String body = buildTopicJson(
                "Tema ok",
                null,
                AccountProfileTypeEnum.SPEAKER,
                endDateJsonSuffix(FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY)
        );

        mockMvc.perform(
                        post(accountTopicsPath(999999L))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
    }

    @Test
    @DisplayName("POST .../topics sem profile_type (só title) retorna 201 como ANNOTATOR")
    void createTopicWithOnlyTemaInfersAnnotatorReturnsCreated() throws Exception {
        String email = "topic-ant-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));

        String body = buildTopicCreateJsonImplicitAnnotator("Tema valido", null);

        String response = mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.title").value("Tema valido"))
                .andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(jsonPath("$.end_date").doesNotExist())
                .andExpect(jsonPath("$.profile_type").value(AccountProfileTypeEnum.ANNOTATOR.name()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long topicId = objectMapper.readTree(response).get("id").asLong();
        assertEquals(0L, topicHistoryRepository.countByTopic_Id(topicId));

        assertEquals(1L, topicRepositoryJpa.countByAccount_Id(accountId));
    }

    @Test
    @DisplayName("POST .../topics com profile_type ANNOTATOR explicito retorna 201")
    void createTopicWithExplicitTipoPerfilAnnotatorReturnsCreated() throws Exception {
        String email = "topic-ant-exp-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));

        String body = buildTopicJson("Tema expl", null, AccountProfileTypeEnum.ANNOTATOR);

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profile_type").value(AccountProfileTypeEnum.ANNOTATOR.name()));
    }

    @Test
    @DisplayName("POST .../topics end_date sem profile_type retorna 400")
    void createTopicWithEndDateButNoProfileTypeReturnsBadRequest() throws Exception {
        String email = "topic-no-prf-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));

        String body = """
                {
                  "title": "Ok tema",
                  "end_date": "2099-12-31T23:59:59"
                }
                """;

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(TopicValidationConstants.MESSAGE_PROFILE_TYPE_REQUIRED_WHEN_END_DATE_PRESENT)
                );
    }

    @Test
    @DisplayName("POST .../topics ANNOTATOR com end_date no body retorna 400")
    void createTopicWithAnnotatorAndEndDateInBodyReturnsBadRequest() throws Exception {
        String email = "topic-ant-ed-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));

        String body = buildTopicJson(
                "Tema valido",
                null,
                AccountProfileTypeEnum.ANNOTATOR,
                endDateJsonSuffix("2099-12-31T23:59:59")
        );

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_ANNOTATOR_TOPIC_CANNOT_SEND_END_DATE));

        assertEquals(0L, topicRepositoryJpa.countByAccount_Id(accountId));
    }

    @Test
    @DisplayName("POST .../topics SPEAKER com end_date apos o limite de 3 dias retorna 400")
    void createTopicWithSpeakerEndDateAfterMaxReturnsBadRequest() throws Exception {
        String email = "topic-spk-late-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);

        String body = buildTopicJson(
                "Tema valido",
                null,
                AccountProfileTypeEnum.SPEAKER,
                endDateJsonSuffix(FIXED_NOW_END_DATE_INVALID_AFTER_MAX)
        );

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        TopicValidationConstants.MESSAGE_END_DATE_MUST_NOT_EXCEED_CREATION_PLUS_MAX_DAYS.formatted(
                                TopicCreationConstants.NON_ANNOTATOR_END_DATE_MAX_OFFSET_DAYS
                        )
                ));

        assertEquals(0L, topicRepositoryJpa.countByAccount_Id(accountId));
    }

    @Test
    @DisplayName("POST .../topics SPEAKER com end_date exatamente no limite (criacao + 3 dias, mesmo horario) retorna 201")
    void createTopicWithSpeakerEndDateExactlyAtMaxReturnsCreated() throws Exception {
        String email = "topic-spk-max-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);

        String body = buildTopicJson(
                "Tema no limite",
                null,
                AccountProfileTypeEnum.SPEAKER,
                endDateJsonSuffix(FIXED_NOW_END_DATE_VALID_AT_MAX)
        );

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.end_date").value(FIXED_NOW_END_DATE_VALID_AT_MAX))
                .andExpect(jsonPath("$.status").value(TopicStatusEnum.OPEN.name()));

        assertEquals(1L, topicRepositoryJpa.countByAccount_Id(accountId));
    }

    @Test
    @DisplayName("POST .../topics SPEAKER sem end_date no body retorna 400")
    void createTopicWithSpeakerMissingEndDateReturnsBadRequest() throws Exception {
        String email = "topic-spk-noed-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);

        String body = buildTopicJson("Tema valido", null, AccountProfileTypeEnum.SPEAKER);

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_END_DATE_REQUIRED_FOR_NON_ANNOTATOR_TOPIC));

        assertEquals(0L, topicRepositoryJpa.countByAccount_Id(accountId));
    }

    @Test
    @DisplayName("POST .../topics SPEAKER com end_date antes da criacao retorna 400")
    void createTopicWithSpeakerEndDateBeforeCreationReturnsBadRequest() throws Exception {
        String email = "topic-spk-early-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);

        String body = buildTopicJson(
                "Tema valido",
                null,
                AccountProfileTypeEnum.SPEAKER,
                endDateJsonSuffix(FIXED_NOW_END_DATE_INVALID_BEFORE)
        );

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_END_DATE_MUST_NOT_BE_BEFORE_CREATION));

        assertEquals(0L, topicRepositoryJpa.countByAccount_Id(accountId));
    }

    @Test
    @DisplayName("POST .../topics com tema apenas espacos retorna 400 (validacao)")
    void createTopicWithBlankTemaReturnsBadRequest() throws Exception {
        String email = "topic-blank-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(26));

        String body = buildTopicJson("   ", null, AccountProfileTypeEnum.ANNOTATOR);

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message").value("title: " + TopicValidationConstants.MESSAGE_TOPIC_TITLE_REQUIRED)
                );
    }

    @Test
    @DisplayName("PUT .../topics/{id} ANNOTATOR apenas tema retorna 200")
    void updateAnnotatorTopicTemaOnlyReturnsOk() throws Exception {
        String email = "topic-put-ant-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        String bodyCreate = buildTopicJson("Tema inicial", null, AccountProfileTypeEnum.ANNOTATOR);
        String created = mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyCreate)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long topicId = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(
                        put(accountTopicItemPath(accountId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildTopicUpdateJsonAnnotatorOnly("Tema atualizado annotator"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tema atualizado annotator"))
                .andExpect(jsonPath("$.profile_type").value(AccountProfileTypeEnum.ANNOTATOR.name()))
                .andExpect(jsonPath("$.lastUpdatedAt").exists());
    }

    @Test
    @DisplayName("PUT .../topics/{id} SPEAKER apenas tema e contexto retorna 200")
    void updateSpeakerTopicWithOnlyTemaAndContextoReturnsOk() throws Exception {
        String email = "topic-put-partial-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        String bodyCreate = buildTopicJson(
                "Titulo original",
                "Ctx orig",
                AccountProfileTypeEnum.SPEAKER,
                endDateJsonSuffix(FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY)
        );
        String created = mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyCreate)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long topicId = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(
                        put(accountTopicItemPath(accountId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildTopicUpdateJsonTemaAndContextoOnly("Só partial", "Novo ctx"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Só partial"))
                .andExpect(jsonPath("$.context").value("Novo ctx"))
                .andExpect(jsonPath("$.end_date").value(FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY))
                .andExpect(jsonPath("$.profile_type").value(AccountProfileTypeEnum.SPEAKER.name()))
                .andExpect(jsonPath("$.lastUpdatedAt").exists());
    }

    @Test
    @DisplayName("PUT .../topics/{id} SPEAKER corpo vazio retorna 400")
    void updateNonAnnotatorTopicEmptyBodyReturnsBadRequest() throws Exception {
        String email = "topic-put-empty-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        long topicId = createSpeakerTopicViaApi(accountId);

        mockMvc.perform(
                        put(accountTopicItemPath(accountId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(TopicValidationConstants.MESSAGE_TOPIC_UPDATE_AT_LEAST_ONE_FIELD));
    }

    @Test
    @DisplayName("PUT .../topics/{id} ANNOTATOR com profile_type no body retorna 400")
    void updateAnnotatorTopicWithProfileTypeInBodyReturnsBadRequest() throws Exception {
        String email = "topic-put-ant-bad-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        String bodyCreate = buildTopicJson("Tema", null, AccountProfileTypeEnum.ANNOTATOR);
        String created = mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyCreate)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long topicId = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(
                        put(accountTopicItemPath(accountId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        buildTopicUpdateJsonNonAnnotator(
                                                "Tema",
                                                null,
                                                AccountProfileTypeEnum.ANNOTATOR,
                                                null
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(TopicValidationConstants.MESSAGE_ANNOTATOR_TOPIC_UPDATE_FORBIDDEN_FIELDS)
                );
    }

    @Test
    @DisplayName("PUT .../topics/{id} SPEAKER nao pode alterar profile_type para ANNOTATOR")
    void updateNonAnnotatorTopicToAnnotatorReturnsBadRequest() throws Exception {
        String email = "topic-put-no-ant-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        long topicId = createSpeakerTopicViaApi(accountId);

        mockMvc.perform(
                        put(accountTopicItemPath(accountId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildTopicUpdateJsonProfileTypeOnly(AccountProfileTypeEnum.ANNOTATOR))
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(TopicValidationConstants.MESSAGE_NON_ANNOTATOR_TOPIC_CANNOT_CHANGE_TO_ANNOTATOR)
                );
    }

    @Test
    @DisplayName("PUT .../topics/{id} SPEAKER dono altera tema e end_date retorna 200")
    void updateSpeakerTopicReturnsOk() throws Exception {
        String email = "topic-put-spk-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        String bodyCreate = buildTopicJson(
                "Tema inicial",
                "Ctx",
                AccountProfileTypeEnum.SPEAKER,
                endDateJsonSuffix(FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY)
        );
        String created = mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyCreate)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long topicId = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(
                        put(accountTopicItemPath(accountId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        buildTopicUpdateJsonNonAnnotator(
                                                "Tema novo",
                                                "Novo contexto",
                                                AccountProfileTypeEnum.SPEAKER,
                                                FIXED_NOW_END_DATE_VALID_AT_MAX
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tema novo"))
                .andExpect(jsonPath("$.context").value("Novo contexto"))
                .andExpect(jsonPath("$.end_date").value(FIXED_NOW_END_DATE_VALID_AT_MAX))
                .andExpect(jsonPath("$.profile_type").value(AccountProfileTypeEnum.SPEAKER.name()))
                .andExpect(jsonPath("$.lastUpdatedAt").exists())
                .andExpect(jsonPath("$.account_name").doesNotExist())
                .andExpect(jsonPath("$.topic_annotator_links").doesNotExist());
    }

    @Test
    @DisplayName("PUT .../topics/{id} outra conta retorna 400 (topico nao pertence ao accountId)")
    void updateTopicWithWrongAccountReturnsBadRequest() throws Exception {
        String emailA = "topic-put-a-" + UUID.randomUUID() + "@email.com";
        Long accountIdA = createAccountAndGetId(emailA, "Ana", "Alfa", LocalDate.now().minusYears(24));
        linkProfileForTopicCreation(accountIdA, AccountProfileTypeEnum.SPEAKER);
        long topicId = createSpeakerTopicViaApi(accountIdA);

        String emailB = "topic-put-b-" + UUID.randomUUID() + "@email.com";
        Long accountIdB = createAccountAndGetId(emailB, "Bea", "Beta", LocalDate.now().minusYears(26));
        linkProfileForTopicCreation(accountIdB, AccountProfileTypeEnum.SPEAKER);

        mockMvc.perform(
                        put(accountTopicItemPath(accountIdB, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        buildTopicUpdateJsonNonAnnotator(
                                                "X",
                                                null,
                                                AccountProfileTypeEnum.SPEAKER,
                                                FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message").value(TopicValidationConstants.MESSAGE_TOPIC_NOT_FOUND_OR_NOT_OWNED)
                );
    }

    @Test
    @DisplayName("PUT .../topics/{id} SPEAKER end_date alem de created_at+3d (topico criado ontem) retorna 400")
    void updateSpeakerTopicEndDateBeyondOriginalCreationWindowReturnsBadRequest() throws Exception {
        String email = "topic-put-window-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        LocalDateTime topicCreatedAt = LocalDateTime.parse(TOPIC_ANCHOR_ONE_DAY_BEFORE_FIXED_NOW);
        LocalDateTime initialEnd = topicCreatedAt.plusDays(1);
        long topicId = persistSpeakerTopicInDb(accountId, topicCreatedAt, initialEnd);

        mockMvc.perform(
                        put(accountTopicItemPath(accountId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        buildTopicUpdateJsonEndDateOnly(
                                                END_DATE_INVALID_AFTER_TOPIC_ANCHOR_PLUS_THREE_DAYS
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        TopicValidationConstants.MESSAGE_END_DATE_MUST_NOT_EXCEED_CREATION_PLUS_MAX_DAYS.formatted(
                                TopicCreationConstants.NON_ANNOTATOR_END_DATE_MAX_OFFSET_DAYS
                        )
                ));

        mockMvc.perform(
                        put(accountTopicItemPath(accountId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        buildTopicUpdateJsonEndDateOnly(
                                                END_DATE_ON_MAX_RELATIVE_TO_TOPIC_ANCHOR
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.end_date").value(END_DATE_ON_MAX_RELATIVE_TO_TOPIC_ANCHOR));
    }

    @Test
    @DisplayName("PUT .../topics/{id} conta INACTIVE retorna 400")
    void updateTopicWithInactiveAccountReturnsBadRequest() throws Exception {
        String email = "topic-put-inact-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(22));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        long topicId = createSpeakerTopicViaApi(accountId);
        mockMvc.perform(patch(accountDeactivatePath(accountId)))
                .andExpect(status().isOk());

        mockMvc.perform(
                        put(accountTopicItemPath(accountId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        buildTopicUpdateJsonNonAnnotator(
                                                "Tema",
                                                null,
                                                AccountProfileTypeEnum.SPEAKER,
                                                FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS)
                );
    }

    @Test
    @DisplayName("GET .../topics sem topicos para a conta retorna 200 e content vazio")
    void getAllTopicsByAccountWhenEmptyReturnsEmptyContent() throws Exception {
        String email = "topic-list-empty-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(24));

        mockMvc.perform(get(accountTopicsPath(accountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("GET .../topics retorna apenas topicos da conta do path (outra conta com topicos nao aparece)")
    void getAllTopicsByAccountReturnsOnlyTopicsForPathAccount() throws Exception {
        String emailA = "topic-list-a-" + UUID.randomUUID() + "@email.com";
        Long accountIdA = createAccountAndGetId(emailA, "Ana", "Alfa", LocalDate.now().minusYears(24));
        linkProfileForTopicCreation(accountIdA, AccountProfileTypeEnum.SPEAKER);
        createSpeakerTopicViaApi(accountIdA);

        String emailB = "topic-list-b-" + UUID.randomUUID() + "@email.com";
        Long accountIdB = createAccountAndGetId(emailB, "Bea", "Beta", LocalDate.now().minusYears(26));

        mockMvc.perform(get(accountTopicsPath(accountIdB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET .../topics com paginacao retorna metadados e ordenacao por criacao descendente")
    void getAllTopicsByAccountReturnsPaginationAndSort() throws Exception {
        String email = "topic-list-page-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        createSpeakerTopicViaApi(accountId);
        createSpeakerTopicViaApi(accountId);
        createSpeakerTopicViaApi(accountId);

        mockMvc.perform(get(accountTopicsPath(accountId)).param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0]." + TopicCreateRequestJsonConstants.TOPIC_ANNOTATOR_LINKS).isArray())
                .andExpect(
                        jsonPath("$.content[0]." + TopicCreateRequestJsonConstants.TOPIC_ANNOTATOR_LINKS + ".length()")
                                .value(0)
                )
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get(accountTopicsPath(accountId)).param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("GET .../topics com conta inexistente retorna 400")
    void getAllTopicsByUnknownAccountReturnsBadRequest() throws Exception {
        mockMvc.perform(get(accountTopicsPath(999999L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
    }

    @Test
    @DisplayName("GET .../topics com page negativo retorna 400")
    void getAllTopicsWithNegativePageReturnsBadRequest() throws Exception {
        String email = "topic-list-badpg-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(24));

        mockMvc.perform(get(accountTopicsPath(accountId)).param("page", "-1").param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_TOPIC_PAGE_NUMBER_NEGATIVE));
    }

    @Test
    @DisplayName("GET .../topics com size acima do maximo retorna 400")
    void getAllTopicsWithSizeAboveMaxReturnsBadRequest() throws Exception {
        String email = "topic-list-badsize-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(24));
        int tooLarge = TopicPaginationConstants.MAX_PAGE_SIZE + 1;

        mockMvc.perform(get(accountTopicsPath(accountId)).param("page", "0").param("size", String.valueOf(tooLarge)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        TopicValidationConstants.MESSAGE_TOPIC_PAGE_SIZE_INVALID.formatted(
                                TopicPaginationConstants.MIN_PAGE_SIZE,
                                TopicPaginationConstants.MAX_PAGE_SIZE
                        )
                ));
    }

    @Test
    @DisplayName("GET .../topics/{topicId} dono do topico retorna 200")
    void getTopicByIdWhenOwnerReturnsOk() throws Exception {
        String email = "topic-get-ok-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        long topicId = createSpeakerTopicViaApi(accountId);

        mockMvc.perform(get(accountTopicItemPath(accountId, topicId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(topicId))
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$." + TopicCreateRequestJsonConstants.ACCOUNT_NAME).value("Nome Sobrenome"))
                .andExpect(jsonPath("$.title").value("Topico api"))
                .andExpect(jsonPath("$.status").value(TopicStatusEnum.OPEN.name()))
                .andExpect(jsonPath("$." + TopicCreateRequestJsonConstants.TOPIC_ANNOTATOR_LINKS).isArray())
                .andExpect(jsonPath("$." + TopicCreateRequestJsonConstants.TOPIC_ANNOTATOR_LINKS + ".length()").value(0));
    }

    @Test
    @DisplayName("GET .../topics/{topicId} inclui topic_annotator_links com id, nome, resume e status da conta anotadora")
    void getTopicByIdIncludesAnnotatorLinksFromTopicAnnotatorLinkTable() throws Exception {
        String emailOwner = "topic-ant-own-" + UUID.randomUUID() + "@email.com";
        Long ownerId = createAccountAndGetId(emailOwner, "Dono", "Speaker", LocalDate.now().minusYears(28));
        linkProfileForTopicCreation(ownerId, AccountProfileTypeEnum.SPEAKER);

        String emailAnnotator = "topic-ant-" + UUID.randomUUID() + "@email.com";
        Long annotatorId = createAccountAndGetId(emailAnnotator, "Ana", "Notator", LocalDate.now().minusYears(30));

        long topicId = createSpeakerTopicViaApi(ownerId);

        mockMvc.perform(
                        post(annotatorLinkPath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkJsonAnnotatorOnly(annotatorId))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(get(accountTopicItemPath(ownerId, topicId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + TopicCreateRequestJsonConstants.TOPIC_ANNOTATOR_LINKS + ".length()").value(1))
                .andExpect(jsonPath(
                        "$." + TopicCreateRequestJsonConstants.TOPIC_ANNOTATOR_LINKS + "[0]."
                                + AnnotatorLinkJsonConstants.ANNOTATOR_ACCOUNT_ID
                ).value(annotatorId.intValue()))
                .andExpect(jsonPath(
                        "$." + TopicCreateRequestJsonConstants.TOPIC_ANNOTATOR_LINKS + "[0]."
                                + AnnotatorLinkJsonConstants.ANNOTATOR_FULL_NAME
                ).value("Ana Notator"))
                .andExpect(jsonPath(
                        "$." + TopicCreateRequestJsonConstants.TOPIC_ANNOTATOR_LINKS + "[0]."
                                + AnnotatorLinkJsonConstants.RESUME
                ).value(nullValue()))
                .andExpect(jsonPath(
                        "$." + TopicCreateRequestJsonConstants.TOPIC_ANNOTATOR_LINKS + "[0]."
                                + AnnotatorLinkJsonConstants.ANNOTATOR_ACCOUNT_STATUS
                ).value(AccountStatusEnum.ACTIVE.name()));

        String resumeText = "Resumo no GET do topico.";
        mockMvc.perform(
                        put(annotatorLinkResumePath(ownerId, topicId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAnnotatorLinkResumeJson(annotatorId, resumeText))
                )
                .andExpect(status().isOk());

        mockMvc.perform(get(accountTopicItemPath(ownerId, topicId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$." + TopicCreateRequestJsonConstants.TOPIC_ANNOTATOR_LINKS + "[0]."
                                + AnnotatorLinkJsonConstants.RESUME
                ).value(resumeText));
    }

    @Test
    @DisplayName("GET .../topics/{topicId} com accountId diferente do criador retorna 400")
    void getTopicByIdWhenWrongAccountReturnsBadRequest() throws Exception {
        String emailA = "topic-get-a-" + UUID.randomUUID() + "@email.com";
        Long accountIdA = createAccountAndGetId(emailA, "Ana", "Alfa", LocalDate.now().minusYears(24));
        linkProfileForTopicCreation(accountIdA, AccountProfileTypeEnum.SPEAKER);
        long topicId = createSpeakerTopicViaApi(accountIdA);

        String emailB = "topic-get-b-" + UUID.randomUUID() + "@email.com";
        Long accountIdB = createAccountAndGetId(emailB, "Bea", "Beta", LocalDate.now().minusYears(26));

        mockMvc.perform(get(accountTopicItemPath(accountIdB, topicId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_TOPIC_NOT_FOUND_OR_NOT_OWNED));
    }

    @Test
    @DisplayName("GET .../topics/{topicId} topico inexistente retorna 400")
    void getTopicByIdWhenUnknownReturnsBadRequest() throws Exception {
        String email = "topic-get-missing-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(24));

        mockMvc.perform(get(accountTopicItemPath(accountId, 999999L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_TOPIC_NOT_FOUND_OR_NOT_OWNED));
    }

    @Test
    @DisplayName("GET .../topics conta INACTIVE retorna 400")
    void getAllTopicsWhenAccountInactiveReturnsBadRequest() throws Exception {
        String email = "topic-getall-inact-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(22));
        mockMvc.perform(patch(accountDeactivatePath(accountId)))
                .andExpect(status().isOk());

        mockMvc.perform(get(accountTopicsPath(accountId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS));
    }

    @Test
    @DisplayName("GET .../topics/{topicId} conta INACTIVE retorna 400")
    void getTopicByIdWhenAccountInactiveReturnsBadRequest() throws Exception {
        String email = "topic-getid-inact-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(22));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        long topicId = createSpeakerTopicViaApi(accountId);
        mockMvc.perform(patch(accountDeactivatePath(accountId)))
                .andExpect(status().isOk());

        mockMvc.perform(get(accountTopicItemPath(accountId, topicId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS));
    }

    @Test
    @DisplayName("GET .../topics?status= retorna só topicos da conta com aquele status (paginado)")
    void getTopicsByStatusQueryReturnsOnlyMatchingForAccount() throws Exception {
        String email = "topic-by-st-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        createSpeakerTopicViaApi(accountId);
        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildTopicCreateJsonImplicitAnnotator("Annotator side", null))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get(accountTopicsPath(accountId))
                                .param(TopicListQueryConstants.STATUS, TopicStatusEnum.OPEN.name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value(TopicStatusEnum.OPEN.name()));

        mockMvc.perform(
                        get(accountTopicsPath(accountId))
                                .param(TopicListQueryConstants.STATUS, TopicStatusEnum.CLOSED.name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET .../topics?status= conta INACTIVE retorna 400")
    void getTopicsWithStatusFilterWhenAccountInactiveReturnsBadRequest() throws Exception {
        String email = "topic-byst-inact-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(22));
        mockMvc.perform(patch(accountDeactivatePath(accountId)))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get(accountTopicsPath(accountId))
                                .param(TopicListQueryConstants.STATUS, TopicStatusEnum.OPEN.name())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS));
    }

    @Test
    @DisplayName("GET .../topics?profile_type= retorna só topicos da conta com aquele perfil de criacao (paginado)")
    void getTopicsByProfileTypeQueryReturnsOnlyMatchingForAccount() throws Exception {
        String email = "topic-by-prf-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildTopicCreateJsonImplicitAnnotator("Só annotator", null))
                )
                .andExpect(status().isCreated());
        createSpeakerTopicViaApi(accountId);

        mockMvc.perform(
                        get(accountTopicsPath(accountId))
                                .param(TopicListQueryConstants.PROFILE_TYPE, AccountProfileTypeEnum.SPEAKER.name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].profile_type").value(AccountProfileTypeEnum.SPEAKER.name()));

        mockMvc.perform(
                        get(accountTopicsPath(accountId))
                                .param(TopicListQueryConstants.PROFILE_TYPE, AccountProfileTypeEnum.COACH.name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET .../topics?profile_type= conta INACTIVE retorna 400")
    void getTopicsWithProfileFilterWhenAccountInactiveReturnsBadRequest() throws Exception {
        String email = "topic-bypr-inact-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(22));
        mockMvc.perform(patch(accountDeactivatePath(accountId)))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get(accountTopicsPath(accountId))
                                .param(TopicListQueryConstants.PROFILE_TYPE, AccountProfileTypeEnum.ANNOTATOR.name())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS));
    }

    @Test
    @DisplayName("GET .../topics?status=&profile_type= aplica os dois filtros juntos (AND)")
    void getTopicsByStatusAndProfileTypeTogetherReturnsIntersection() throws Exception {
        String email = "topic-both-f-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.COACH);
        createSpeakerTopicViaApi(accountId);
        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        buildTopicJson(
                                                "Topico coach",
                                                null,
                                                AccountProfileTypeEnum.COACH,
                                                endDateJsonSuffix(FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY)
                                        )
                                )
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get(accountTopicsPath(accountId))
                                .param(TopicListQueryConstants.STATUS, TopicStatusEnum.OPEN.name())
                                .param(TopicListQueryConstants.PROFILE_TYPE, AccountProfileTypeEnum.SPEAKER.name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].profile_type").value(AccountProfileTypeEnum.SPEAKER.name()))
                .andExpect(jsonPath("$.content[0].status").value(TopicStatusEnum.OPEN.name()));

        mockMvc.perform(
                        get(accountTopicsPath(accountId))
                                .param(TopicListQueryConstants.STATUS, TopicStatusEnum.OPEN.name())
                                .param(TopicListQueryConstants.PROFILE_TYPE, AccountProfileTypeEnum.COACH.name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].profile_type").value(AccountProfileTypeEnum.COACH.name()));

        mockMvc.perform(
                        get(accountTopicsPath(accountId))
                                .param(TopicListQueryConstants.STATUS, TopicStatusEnum.OPEN.name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("GET .../topics com profile_type=ANNOTATOR e status juntos retorna 400 (operacao invalida)")
    void getTopicsWithAnnotatorProfileAndStatusFilterReturnsBadRequest() throws Exception {
        String email = "topic-ant-st-filt-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(24));

        mockMvc.perform(
                        get(accountTopicsPath(accountId))
                                .param(TopicListQueryConstants.PROFILE_TYPE, AccountProfileTypeEnum.ANNOTATOR.name())
                                .param(TopicListQueryConstants.STATUS, TopicStatusEnum.OPEN.name())
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message").value(
                                TopicValidationConstants.MESSAGE_ANNOTATOR_TOPIC_LIST_CANNOT_COMBINE_WITH_STATUS_FILTER
                        )
                );
    }

    @Test
    @DisplayName("PATCH .../topics/{topicId}/close com topico OPEN retorna 200 e status CLOSED")
    void closeOpenTopicReturnsOk() throws Exception {
        String email = "topic-close-ok-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        long topicId = createSpeakerTopicViaApi(accountId);

        assertEquals(1L, topicHistoryRepository.countByTopic_Id(topicId));
        assertEquals(
                TopicStatusEnum.OPEN,
                topicHistoryRepository.findByTopic_IdOrderByIdAsc(topicId).get(0).getStatus()
        );

        mockMvc.perform(patch(accountTopicClosePath(accountId, topicId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(topicId))
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.status").value(TopicStatusEnum.CLOSED.name()))
                .andExpect(jsonPath("$.lastUpdatedAt").exists());

        assertEquals(2L, topicHistoryRepository.countByTopic_Id(topicId));
        var historyAfterClose = topicHistoryRepository.findByTopic_IdOrderByIdAsc(topicId);
        assertEquals(TopicStatusEnum.CLOSED, historyAfterClose.get(1).getStatus());
        assertNotNull(historyAfterClose.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("PATCH .../topics/{topicId}/close com outra conta retorna 400")
    void closeTopicWithWrongAccountReturnsBadRequest() throws Exception {
        String emailA = "topic-close-a-" + UUID.randomUUID() + "@email.com";
        Long accountIdA = createAccountAndGetId(emailA, "Ana", "Alfa", LocalDate.now().minusYears(24));
        linkProfileForTopicCreation(accountIdA, AccountProfileTypeEnum.SPEAKER);
        long topicId = createSpeakerTopicViaApi(accountIdA);

        String emailB = "topic-close-b-" + UUID.randomUUID() + "@email.com";
        Long accountIdB = createAccountAndGetId(emailB, "Bea", "Beta", LocalDate.now().minusYears(26));
        linkProfileForTopicCreation(accountIdB, AccountProfileTypeEnum.SPEAKER);

        mockMvc.perform(patch(accountTopicClosePath(accountIdB, topicId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_TOPIC_NOT_FOUND_OR_NOT_OWNED));
    }

    @Test
    @DisplayName("PATCH .../topics/{topicId}/close conta INACTIVE retorna 400")
    void closeTopicWhenAccountInactiveReturnsBadRequest() throws Exception {
        String email = "topic-close-inact-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(22));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        long topicId = createSpeakerTopicViaApi(accountId);
        mockMvc.perform(patch(accountDeactivatePath(accountId)))
                .andExpect(status().isOk());

        mockMvc.perform(patch(accountTopicClosePath(accountId, topicId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS));
    }

    @Test
    @DisplayName("PATCH .../topics/{topicId}/close com status CANCELED retorna 400")
    void closeTopicWhenCanceledReturnsBadRequest() throws Exception {
        String email = "topic-close-can-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        long topicId = persistSpeakerTopicWithStatus(accountId, TopicStatusEnum.CANCELED);

        mockMvc.perform(patch(accountTopicClosePath(accountId, topicId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_TOPIC_CLOSE_ONLY_ALLOWED_FROM_OPEN));
    }

    @Test
    @DisplayName("PATCH .../topics/{topicId}/close com status REVIEWED retorna 400")
    void closeTopicWhenReviewedReturnsBadRequest() throws Exception {
        String email = "topic-close-rev-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        long topicId = persistSpeakerTopicWithStatus(accountId, TopicStatusEnum.REVIEWED);

        mockMvc.perform(patch(accountTopicClosePath(accountId, topicId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_TOPIC_CLOSE_ONLY_ALLOWED_FROM_OPEN));
    }

    @Test
    @DisplayName("PATCH .../topics/{topicId}/close com status ja CLOSED retorna 400")
    void closeTopicWhenAlreadyClosedReturnsBadRequest() throws Exception {
        String email = "topic-close-clsd-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.SPEAKER);
        long topicId = persistSpeakerTopicWithStatus(accountId, TopicStatusEnum.CLOSED);

        mockMvc.perform(patch(accountTopicClosePath(accountId, topicId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_TOPIC_CLOSE_ONLY_ALLOWED_FROM_OPEN));
    }

    @Test
    @DisplayName("PATCH .../topics/{topicId}/close topico ANNOTATOR sem status retorna 400")
    void closeAnnotatorTopicWithoutStatusReturnsBadRequest() throws Exception {
        String email = "topic-close-ant-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        String created = mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildTopicCreateJsonImplicitAnnotator("Só annotator close", null))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long topicId = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(patch(accountTopicClosePath(accountId, topicId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TopicValidationConstants.MESSAGE_TOPIC_CLOSE_ONLY_ALLOWED_FROM_OPEN));
    }

    @Test
    @DisplayName("POST .../topics normaliza tema com espacos nas extremidades")
    void createTopicTrimsTemaEdges() throws Exception {
        String email = "topic-trim-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(27));
        linkProfileForTopicCreation(accountId, AccountProfileTypeEnum.BISHOP);

        String body = buildTopicJson(
                "  Ab  ",
                null,
                AccountProfileTypeEnum.BISHOP,
                endDateJsonSuffix(FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY)
        );

        mockMvc.perform(
                        post(accountTopicsPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Ab"));
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

    private long persistSpeakerTopicWithStatus(Long accountId, TopicStatusEnum status) {
        Account account = accountRepositoryJpa.findById(accountId).orElseThrow();
        LocalDateTime createdAt = LocalDateTime.parse(SPEAKER_TOPIC_DB_DEFAULT_CREATED_AT);
        LocalDateTime endDate = LocalDateTime.parse(FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY);
        Topic topic = Topic.builder()
                .account(account)
                .title("Persistido status " + status.name())
                .context(null)
                .createdAt(createdAt)
                .lastUpdatedAt(null)
                .status(status)
                .endDate(endDate)
                .profileType(AccountProfileTypeEnum.SPEAKER)
                .build();
        return topicRepositoryJpa.save(topic).getId();
    }

    private long createSpeakerTopicViaApi(Long accountId) throws Exception {
        String bodyCreate = buildTopicJson(
                "Topico api",
                null,
                AccountProfileTypeEnum.SPEAKER,
                endDateJsonSuffix(FIXED_NOW_END_DATE_VALID_PLUS_ONE_DAY)
        );
        String created = mockMvc.perform(
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

    private long persistSpeakerTopicInDb(Long accountId, LocalDateTime createdAt, LocalDateTime endDate) {
        Account account = accountRepositoryJpa.findById(accountId).orElseThrow();
        Topic topic = Topic.builder()
                .account(account)
                .title("Persistido para PUT")
                .context(null)
                .createdAt(createdAt)
                .lastUpdatedAt(null)
                .status(TopicStatusEnum.OPEN)
                .endDate(endDate)
                .profileType(AccountProfileTypeEnum.SPEAKER)
                .build();
        return topicRepositoryJpa.save(topic).getId();
    }

    private static String buildTopicUpdateJsonAnnotatorOnly(String title) {
        return """
                {"%s": "%s"}
                """
                .formatted(TopicCreateRequestJsonConstants.TITLE, escapeJson(title));
    }

    private static String buildTopicUpdateJsonTemaAndContextoOnly(String title, String contexto) {
        return """
                {
                  "%s": "%s",
                  "%s": "%s"
                }
                """
                .formatted(
                        TopicCreateRequestJsonConstants.TITLE,
                        escapeJson(title),
                        TopicCreateRequestJsonConstants.CONTEXT,
                        escapeJson(contexto));
    }

    private static String buildTopicUpdateJsonEndDateOnly(String endDateIso) {
        return """
                {"end_date": "%s"}
                """.formatted(endDateIso);
    }

    private static String buildTopicUpdateJsonProfileTypeOnly(AccountProfileTypeEnum profileType) {
        return """
                {"profile_type": "%s"}
                """.formatted(profileType.name());
    }

    private static String buildTopicUpdateJsonNonAnnotator(
            String title,
            String contexto,
            AccountProfileTypeEnum profileType,
            String endDateIsoOrNull
    ) {
        String endPart = endDateIsoOrNull == null ? "" : ", \"end_date\": \"" + endDateIsoOrNull + "\"";
        if (contexto == null) {
            return """
                    {
                      "%s": "%s",
                      "profile_type": "%s"%s
                    }
                    """
                    .formatted(
                            TopicCreateRequestJsonConstants.TITLE,
                            escapeJson(title),
                            profileType.name(),
                            endPart
                    );
        }
        return """
                {
                  "%s": "%s",
                  "%s": "%s",
                  "profile_type": "%s"%s
                }
                """
                .formatted(
                        TopicCreateRequestJsonConstants.TITLE,
                        escapeJson(title),
                        TopicCreateRequestJsonConstants.CONTEXT,
                        escapeJson(contexto),
                        profileType.name(),
                        endPart
                );
    }

    private static String accountDeactivatePath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.ACCOUNT_DEACTIVATE_SEGMENT;
    }

    private static String accountProfilesPath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.ACCOUNT_PROFILES_SEGMENT;
    }

    private static String buildLinkProfileJson(AccountProfileTypeEnum profileType) {
        return """
                {"profileType": "%s"}
                """.formatted(profileType.name());
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

    private static String buildTopicJson(String tema, String contexto, AccountProfileTypeEnum profileType) {
        return buildTopicJson(tema, contexto, profileType, "");
    }

    /**
     * @param extraCommaPrefixedJson trecho JSON apos profile_type, com virgula inicial (ex.: {@code , "end_date": "..."}).
     */
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
                    """.formatted(
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
                """.formatted(
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
