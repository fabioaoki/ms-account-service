package br.com.mechanic.account;

import br.com.mechanic.account.constant.AccountPresentationSummaryJsonConstants;
import br.com.mechanic.account.constant.AccountPresentationSummaryValidationConstants;
import br.com.mechanic.account.constant.AccountProfileLinkValidationConstants;
import br.com.mechanic.account.constant.AccountRegistrationValidationConstants;
import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.AuthValidationConstants;
import br.com.mechanic.account.constant.AccountUpdateValidationConstants;
import br.com.mechanic.account.constant.ExceptionMessageConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.account.AccountStatusHistory;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import br.com.mechanic.account.repository.account.AccountHistoryRepository;
import br.com.mechanic.account.repository.account.AccountProfileRepository;
import br.com.mechanic.account.repository.account.AccountStatusHistoryRepository;
import br.com.mechanic.account.repository.account.impl.AccountStatusHistoryRepositoryJpa;
import br.com.mechanic.account.repository.account.jpa.AccountRepositoryJpa;
import br.com.mechanic.account.repository.profile.ProfileRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerIntegrationTest {

    private static final String PASSWORD_VALID = "secret123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepositoryJpa accountRepositoryJpa;

    @Autowired
    private AccountProfileRepository accountProfileRepository;

    @Autowired
    private AccountHistoryRepository accountHistoryRepository;

    @Autowired
    private AccountStatusHistoryRepository accountStatusHistoryRepository;

    @SpyBean
    private AccountStatusHistoryRepositoryJpa accountStatusHistoryRepositoryJpa;

    @Autowired
    private ProfileRepository profileRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void resetAccountStatusHistoryRepositorySpy() {
        reset(accountStatusHistoryRepositoryJpa);
    }

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
                .andExpect(jsonPath("$.status").value(AccountStatusEnum.ACTIVE.name()))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("POST /api/v1/accounts nao grava em account_status_history")
    void createAccountDoesNotPersistAccountStatusHistoryRow() throws Exception {
        String email = "nohist-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));

        assertEquals(0L, accountStatusHistoryRepository.countByAccount_Id(accountId));
        verify(accountStatusHistoryRepositoryJpa, never()).save(any(AccountStatusHistory.class));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{accountId} retorna nome, birthDate, accountId, createdAt, status e profileTypes")
    void getAccountByIdReturnsDetailWithLinkedProfiles() throws Exception {
        String email = "get-" + UUID.randomUUID() + "@email.com";
        LocalDate birthDate = LocalDate.now().minusYears(25);
        Long accountId = createAccountAndGetId(email, "fabio", "de carvalho", birthDate);

        mockMvc.perform(get(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.name").value("Fabio de Carvalho"))
                .andExpect(jsonPath("$.birthDate").value(birthDate.toString()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.status").value(AccountStatusEnum.ACTIVE.name()))
                .andExpect(jsonPath("$.profileTypes.length()").value(1))
                .andExpect(jsonPath("$.profileTypes[0]").value(AccountProfileTypeEnum.ANNOTATOR.name()));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{accountId} apos vincular SPEAKER retorna perfis na ordem de vinculo")
    void getAccountByIdReturnsProfileTypesInLinkOrder() throws Exception {
        String email = "get2-" + UUID.randomUUID() + "@email.com";
        LocalDate birthDate = LocalDate.now().minusYears(28);
        Long accountId = createAccountAndGetId(email, "ana", "silva", birthDate);

        mockMvc.perform(
                        post(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.SPEAKER))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(get(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(AccountStatusEnum.ACTIVE.name()))
                .andExpect(jsonPath("$.profileTypes.length()").value(2))
                .andExpect(jsonPath("$.profileTypes[0]").value(AccountProfileTypeEnum.ANNOTATOR.name()))
                .andExpect(jsonPath("$.profileTypes[1]").value(AccountProfileTypeEnum.SPEAKER.name()));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{accountId} com id inexistente retorna 400")
    void getAccountByIdWithNonExistingIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + 999999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
    }

    @Test
    @DisplayName("PATCH .../deactivate com conta ACTIVE retorna 200 sem corpo e persiste INACTIVE")
    void deactivateActiveAccountReturnsOkAndSetsInactive() throws Exception {
        String email = "deact-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(22));

        mockMvc.perform(patch(accountDeactivatePath(accountId)))
                .andExpect(status().isOk());

        assertEquals(
                AccountStatusEnum.INACTIVE,
                accountRepositoryJpa.findById(accountId).orElseThrow().getStatus());

        verify(accountStatusHistoryRepositoryJpa, times(1)).save(argThat(history ->
                history.getAccount().getId().equals(accountId)
                        && history.getStatus() == AccountStatusEnum.INACTIVE));
    }

    @Test
    @DisplayName("PATCH .../deactivate com conta ja INACTIVE retorna 200 (idempotente)")
    void deactivateAlreadyInactiveReturnsOkIdempotent() throws Exception {
        String email = "deact2-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(22));

        mockMvc.perform(patch(accountDeactivatePath(accountId))).andExpect(status().isOk());
        mockMvc.perform(patch(accountDeactivatePath(accountId))).andExpect(status().isOk());

        assertEquals(
                AccountStatusEnum.INACTIVE,
                accountRepositoryJpa.findById(accountId).orElseThrow().getStatus());

        verify(accountStatusHistoryRepositoryJpa, times(1)).save(any(AccountStatusHistory.class));
    }

    @Test
    @DisplayName("PATCH .../activate com conta INACTIVE retorna 200 sem corpo e persiste ACTIVE")
    void activateInactiveAccountReturnsOkAndSetsActive() throws Exception {
        String email = "act-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(22));

        mockMvc.perform(patch(accountDeactivatePath(accountId))).andExpect(status().isOk());

        mockMvc.perform(patch(accountActivatePath(accountId))).andExpect(status().isOk());

        assertEquals(
                AccountStatusEnum.ACTIVE,
                accountRepositoryJpa.findById(accountId).orElseThrow().getStatus());

        verify(accountStatusHistoryRepositoryJpa, times(2)).save(any(AccountStatusHistory.class));
    }

    @Test
    @DisplayName("PATCH .../activate com conta ja ACTIVE retorna 200 (idempotente)")
    void activateAlreadyActiveReturnsOkIdempotent() throws Exception {
        String email = "act2-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(22));

        mockMvc.perform(patch(accountActivatePath(accountId))).andExpect(status().isOk());

        mockMvc.perform(patch(accountActivatePath(accountId))).andExpect(status().isOk());

        assertEquals(
                AccountStatusEnum.ACTIVE,
                accountRepositoryJpa.findById(accountId).orElseThrow().getStatus());

        verify(accountStatusHistoryRepositoryJpa, never()).save(any(AccountStatusHistory.class));
    }

    @Test
    @DisplayName("PATCH .../deactivate com id inexistente retorna 400")
    void deactivateNonExistingAccountReturnsBadRequest() throws Exception {
        mockMvc.perform(patch(accountDeactivatePath(999999L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));

        verify(accountStatusHistoryRepositoryJpa, never()).save(any(AccountStatusHistory.class));
    }

    @Test
    @DisplayName("PATCH .../activate com id inexistente retorna 400")
    void activateNonExistingAccountReturnsBadRequest() throws Exception {
        mockMvc.perform(patch(accountActivatePath(999999L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));

        verify(accountStatusHistoryRepositoryJpa, never()).save(any(AccountStatusHistory.class));
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

    @Test
    @DisplayName("POST /api/v1/accounts/{id}/profiles vincula SPEAKER e persiste account_profile e account_history")
    void linkSpeakerProfileToAccountReturnsCreatedAndPersistsRows() throws Exception {
        String email = "link-spk-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        Long speakerProfileId = profileRepository
                .findByProfileType(AccountProfileTypeEnum.SPEAKER)
                .orElseThrow()
                .getId();

        assertEquals(1L, accountProfileRepository.countByAccount_Id(accountId));
        assertEquals(1L, accountHistoryRepository.countByAccount_Id(accountId));

        mockMvc.perform(
                        post(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.SPEAKER))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.profileId").value(speakerProfileId))
                .andExpect(jsonPath("$.profileType").value(AccountProfileTypeEnum.SPEAKER.name()));

        assertEquals(2L, accountProfileRepository.countByAccount_Id(accountId));
        assertEquals(2L, accountHistoryRepository.countByAccount_Id(accountId));
    }

    @Test
    @DisplayName("POST /api/v1/accounts/{id}/profiles com ANNOTATOR retorna 400")
    void linkAnnotatorProfileReturnsBadRequest() throws Exception {
        String email = "link-ant-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));

        mockMvc.perform(
                        post(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.ANNOTATOR))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(AccountProfileLinkValidationConstants.MESSAGE_ANNOTATOR_CANNOT_BE_LINKED_VIA_THIS_FLOW));
    }

    @Test
    @DisplayName("POST /api/v1/accounts/{id}/profiles com conta INACTIVE retorna 400")
    void linkProfileWithInactiveAccountReturnsBadRequest() throws Exception {
        String email = "link-inact-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));

        Account account = accountRepositoryJpa.findById(accountId).orElseThrow();
        account.setStatus(AccountStatusEnum.INACTIVE);
        accountRepositoryJpa.save(account);

        mockMvc.perform(
                        post(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.SPEAKER))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(AccountProfileLinkValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_PROFILE_BINDING_OPERATIONS));
    }

    @Test
    @DisplayName("DELETE /api/v1/accounts/{id}/profiles remove SPEAKER, grava history e mantem ANNOTATOR")
    void unlinkSpeakerProfileReturnsOkAndPersistsHistory() throws Exception {
        String email = "unlink-spk-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        mockMvc.perform(
                        post(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.SPEAKER))
                )
                .andExpect(status().isCreated());

        assertEquals(2L, accountProfileRepository.countByAccount_Id(accountId));
        assertEquals(2L, accountHistoryRepository.countByAccount_Id(accountId));

        mockMvc.perform(
                        delete(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.SPEAKER))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        assertEquals(1L, accountProfileRepository.countByAccount_Id(accountId));
        assertEquals(3L, accountHistoryRepository.countByAccount_Id(accountId));
    }

    @Test
    @DisplayName("DELETE /api/v1/accounts/{id}/profiles com ANNOTATOR retorna 400")
    void unlinkAnnotatorProfileReturnsBadRequest() throws Exception {
        String email = "unlink-ant-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));

        mockMvc.perform(
                        delete(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.ANNOTATOR))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(AccountProfileLinkValidationConstants.MESSAGE_ANNOTATOR_CANNOT_BE_UNLINKED));
    }

    @Test
    @DisplayName("DELETE /api/v1/accounts/{id}/profiles com perfil nao vinculado retorna 200 idempotente")
    void unlinkProfileNotLinkedReturnsOkIdempotent() throws Exception {
        String email = "unlink-nl-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));
        assertEquals(1L, accountProfileRepository.countByAccount_Id(accountId));
        assertEquals(1L, accountHistoryRepository.countByAccount_Id(accountId));

        mockMvc.perform(
                        delete(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.BISHOP))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        assertEquals(1L, accountProfileRepository.countByAccount_Id(accountId));
        assertEquals(1L, accountHistoryRepository.countByAccount_Id(accountId));
    }

    @Test
    @DisplayName("DELETE /api/v1/accounts/{id}/profiles duas vezes seguidas retorna 200 na segunda sem novo history")
    void unlinkProfileTwiceSecondCallIsIdempotent() throws Exception {
        String email = "unlink-2x-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));

        mockMvc.perform(
                        post(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.SPEAKER))
                )
                .andExpect(status().isCreated());

        assertEquals(2L, accountHistoryRepository.countByAccount_Id(accountId));

        mockMvc.perform(
                        delete(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.SPEAKER))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        assertEquals(3L, accountHistoryRepository.countByAccount_Id(accountId));

        mockMvc.perform(
                        delete(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.SPEAKER))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        assertEquals(3L, accountHistoryRepository.countByAccount_Id(accountId));
    }

    @Test
    @DisplayName("DELETE /api/v1/accounts/{id}/profiles com conta INACTIVE retorna 400")
    void unlinkProfileWithInactiveAccountReturnsBadRequest() throws Exception {
        String email = "unlink-inact-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));

        mockMvc.perform(
                        post(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.COACH))
                )
                .andExpect(status().isCreated());

        Account account = accountRepositoryJpa.findById(accountId).orElseThrow();
        account.setStatus(AccountStatusEnum.INACTIVE);
        accountRepositoryJpa.save(account);

        mockMvc.perform(
                        delete(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.COACH))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(AccountProfileLinkValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_PROFILE_BINDING_OPERATIONS));
    }

    @Test
    @DisplayName("DELETE /api/v1/accounts/{id}/profiles com conta inexistente retorna 400")
    void unlinkProfileFromNonExistingAccountReturnsBadRequest() throws Exception {
        Long accountId = 999999L;

        mockMvc.perform(
                        delete(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.SPEAKER))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
    }

    @Test
    @DisplayName("POST /api/v1/accounts/{id}/profiles com perfil ja vinculado retorna 400")
    void linkDuplicateProfileReturnsBadRequest() throws Exception {
        String email = "link-dup-" + UUID.randomUUID() + "@email.com";
        Long accountId = createAccountAndGetId(email, "nome", "sobrenome", LocalDate.now().minusYears(25));

        mockMvc.perform(
                        post(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.COACH))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.COACH))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(AccountProfileLinkValidationConstants.MESSAGE_PROFILE_ALREADY_LINKED_TO_ACCOUNT));
    }

    @Test
    @DisplayName("POST /api/v1/accounts/{id}/profiles com conta inexistente retorna 400")
    void linkProfileToNonExistingAccountReturnsBadRequest() throws Exception {
        Long accountId = 999999L;

        mockMvc.perform(
                        post(accountProfilesPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildLinkProfileJson(AccountProfileTypeEnum.BISHOP))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
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
    @DisplayName("PATCH /api/v1/accounts/{accountId} com conta INACTIVE retorna 400")
    void updateAccountWithInactiveStatusReturnsBadRequest() throws Exception {
        String email = "inactive-" + UUID.randomUUID() + "@email.com";
        LocalDate birthDate = LocalDate.now().minusYears(25);
        Long accountId = createAccountAndGetId(email, "fabio", "de carvalho", birthDate);

        Account account = accountRepositoryJpa.findById(accountId).orElseThrow();
        account.setStatus(AccountStatusEnum.INACTIVE);
        accountRepositoryJpa.save(account);

        LocalDate newBirthDate = LocalDate.now().minusYears(30);
        String body = buildUpdateJson("ana", "silva", newBirthDate);

        mockMvc.perform(
                        patch(ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(AccountUpdateValidationConstants.MESSAGE_CANNOT_UPDATE_INACTIVE_ACCOUNT));
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

    @Test
    @DisplayName("POST .../presentation-summary com mais de um profileType retorna 201")
    void createAccountPresentationSummaryReturnsCreated() throws Exception {
        Long accountId = createAccountAndGetId(
                "summary-create-" + UUID.randomUUID() + "@email.com",
                "Nome",
                "Resumo",
                LocalDate.now().minusYears(25)
        );
        linkExtraProfile(accountId, AccountProfileTypeEnum.SPEAKER);

        mockMvc.perform(
                        post(accountPresentationSummaryPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAccountPresentationSummaryJson("Apresentação inicial do usuário."))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.summary").value("Apresentação inicial do usuário."))
                .andExpect(jsonPath("$.created_at").exists())
                .andExpect(jsonPath("$.last_updated_at").doesNotExist());
    }

    @Test
    @DisplayName("PUT .../presentation-summary atualiza summary e last_updated_at")
    void updateAccountPresentationSummaryReturnsOk() throws Exception {
        Long accountId = createAccountAndGetId(
                "summary-update-" + UUID.randomUUID() + "@email.com",
                "Nome",
                "Resumo",
                LocalDate.now().minusYears(25)
        );
        linkExtraProfile(accountId, AccountProfileTypeEnum.SPEAKER);

        mockMvc.perform(
                        post(accountPresentationSummaryPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAccountPresentationSummaryJson("Resumo inicial."))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        put(accountPresentationSummaryPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAccountPresentationSummaryJson("Resumo atualizado com mais detalhes."))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.summary").value("Resumo atualizado com mais detalhes."))
                .andExpect(jsonPath("$.last_updated_at").exists());
    }

    @Test
    @DisplayName("GET .../presentation-summary retorna resumo da conta ACTIVE")
    void getAccountPresentationSummaryReturnsOk() throws Exception {
        Long accountId = createAccountAndGetId(
                "summary-get-" + UUID.randomUUID() + "@email.com",
                "Nome",
                "Resumo",
                LocalDate.now().minusYears(25)
        );
        linkExtraProfile(accountId, AccountProfileTypeEnum.SPEAKER);

        mockMvc.perform(
                        post(accountPresentationSummaryPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAccountPresentationSummaryJson("Resumo para leitura."))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(get(accountPresentationSummaryPath(accountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.summary").value("Resumo para leitura."));
    }

    @Test
    @DisplayName("POST .../presentation-summary com accountId inexistente retorna 400")
    void createAccountPresentationSummaryWithUnknownAccountReturnsBadRequest() throws Exception {
        mockMvc.perform(
                        post(accountPresentationSummaryPath(999999L))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAccountPresentationSummaryJson("Resumo qualquer."))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
    }

    @Test
    @DisplayName("POST .../presentation-summary com um único profileType retorna 400")
    void createAccountPresentationSummaryWithSingleProfileReturnsBadRequest() throws Exception {
        Long accountId = createAccountAndGetId(
                "summary-single-profile-" + UUID.randomUUID() + "@email.com",
                "Nome",
                "Resumo",
                LocalDate.now().minusYears(25)
        );

        mockMvc.perform(
                        post(accountPresentationSummaryPath(accountId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(buildAccountPresentationSummaryJson("Resumo sem perfil extra."))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        AccountPresentationSummaryValidationConstants.MESSAGE_ACCOUNT_MUST_HAVE_MORE_THAN_ONE_PROFILE_TYPE
                ));
    }

    private void linkExtraProfile(Long accountId, AccountProfileTypeEnum profileType) throws Exception {
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

    private static String accountProfilesPath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.ACCOUNT_PROFILES_SEGMENT;
    }

    private static String accountDeactivatePath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.ACCOUNT_DEACTIVATE_SEGMENT;
    }

    private static String accountActivatePath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.ACCOUNT_ACTIVATE_SEGMENT;
    }

    private static String buildLinkProfileJson(AccountProfileTypeEnum profileType) {
        return """
                {"profileType": "%s"}
                """.formatted(profileType.name());
    }

    private static String accountPresentationSummaryPath(Long accountId) {
        return ApiPathConstants.ACCOUNTS_BASE_PATH + "/" + accountId + ApiPathConstants.ACCOUNT_PRESENTATION_SUMMARY_SEGMENT;
    }

    private static String buildAccountPresentationSummaryJson(String summary) {
        return """
                {
                  "%s": "%s"
                }
                """
                .formatted(AccountPresentationSummaryJsonConstants.SUMMARY, escapeJsonForPresentationSummaryBody(summary));
    }

    private static String escapeJsonForPresentationSummaryBody(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
