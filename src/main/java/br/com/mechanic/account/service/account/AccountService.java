package br.com.mechanic.account.service.account;

import br.com.mechanic.account.constant.AccountProfileLinkValidationConstants;
import br.com.mechanic.account.constant.AccountRegistrationValidationConstants;
import br.com.mechanic.account.constant.AccountUpdateValidationConstants;
import br.com.mechanic.account.constant.AccountServiceLogConstants;
import br.com.mechanic.account.constant.ExceptionMessageConstants;
import br.com.mechanic.account.constant.ProfileCatalogMessageConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.profile.Profile;
import br.com.mechanic.account.enuns.AccountHistoryActionEnum;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import br.com.mechanic.account.exception.AccountException;
import br.com.mechanic.account.mapper.account.Mapper;
import br.com.mechanic.account.model.account.AccountModel;
import br.com.mechanic.account.repository.account.impl.AccountHistoryRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.AccountProfileRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.AccountRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.AccountStatusHistoryRepositoryImpl;
import br.com.mechanic.account.repository.profile.impl.ProfileRepositoryImpl;
import br.com.mechanic.account.service.request.AccountProfileLinkRequest;
import br.com.mechanic.account.service.request.AccountProfileUnlinkRequest;
import br.com.mechanic.account.service.request.UserCreateRequest;
import br.com.mechanic.account.service.request.AccountUpdateRequest;
import br.com.mechanic.account.service.response.AccountDetailResponse;
import br.com.mechanic.account.service.response.AccountProfileLinkResponse;
import br.com.mechanic.account.service.response.AccountResponse;
import br.com.mechanic.account.security.ApiAccessValidation;
import br.com.mechanic.account.service.response.AccountUpdateResponse;
import br.com.mechanic.account.util.FullNameFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService implements AccountServiceBO {

    private final AccountRepositoryImpl accountRepository;
    private final AccountHistoryRepositoryImpl accountHistoryRepository;
    private final AccountStatusHistoryRepositoryImpl accountStatusHistoryRepository;
    private final AccountProfileRepositoryImpl accountProfileRepository;
    private final ProfileRepositoryImpl profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApiAccessValidation apiAccessValidation;

    @Override
    @Transactional
    public AccountResponse create(UserCreateRequest request) {
        log.info(AccountServiceLogConstants.CREATE_ACCOUNT_FLOW_STARTED);
        validateBirthDate(request.birthDate());
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (accountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            log.warn(AccountServiceLogConstants.CREATE_ACCOUNT_REJECTED_BUSINESS_RULE);
            throw new AccountException(ExceptionMessageConstants.GENERIC_REGISTRATION_FAILURE);
        }
        Profile initialProfile = findRequiredAnnotatorProfile();
        String formattedName = buildFormattedFullName(request);
        AccountModel accountModel = Mapper.toModel(request, normalizedEmail, formattedName, passwordEncoder);
        Account account = Mapper.toEntity(accountModel);
        account.setPublicId(UUID.randomUUID());
        account.setStatus(AccountStatusEnum.ACTIVE);
        Account saved = accountRepository.save(account);
        accountProfileRepository.save(Mapper.toAccountProfile(saved, initialProfile));
        accountHistoryRepository.save(Mapper.toAccountHistory(saved, initialProfile, AccountHistoryActionEnum.LINK));
        log.info(AccountServiceLogConstants.CREATE_ACCOUNT_FLOW_COMPLETED, saved.getId());
        return Mapper.toResponse(saved, initialProfile.getProfileType());
    }

    @Override
    @Transactional
    public AccountUpdateResponse update(Long accountId, AccountUpdateRequest request) {
        log.info(AccountServiceLogConstants.UPDATE_ACCOUNT_FLOW_STARTED, accountId);
        apiAccessValidation.requireOwnerStandardOrFull(accountId);

        boolean hasBirthDate = request.birthDate() != null;
        boolean hasFirstName = request.firstName() != null;
        boolean hasLastName = request.lastName() != null;
        boolean shouldUpdateName = hasFirstName || hasLastName;
        boolean hasAnyFieldToUpdate = hasBirthDate || shouldUpdateName;

        if (!hasAnyFieldToUpdate) {
            throw new AccountException(AccountUpdateValidationConstants.MESSAGE_AT_LEAST_ONE_FIELD_MUST_BE_PROVIDED);
        }

        Account accountToUpdate = getAccountOrThrow(accountId);
        assertAccountAllowsUpdate(accountToUpdate);

        if (shouldUpdateName) {
            String newFirstName = hasFirstName
                    ? validateNameField(request.firstName(), AccountUpdateValidationConstants.MESSAGE_FIRST_NAME_REQUIRED)
                    : accountToUpdate.getFirstName();

            String newLastName = hasLastName
                    ? validateNameField(request.lastName(), AccountUpdateValidationConstants.MESSAGE_LAST_NAME_REQUIRED)
                    : accountToUpdate.getLastName();

            String formattedName = FullNameFormatter.formatFromParts(newFirstName, newLastName);
            accountToUpdate.setFirstName(newFirstName);
            accountToUpdate.setLastName(newLastName);
            accountToUpdate.setName(formattedName);
        }

        validateUpdateAndPersist(accountToUpdate, request.birthDate(), hasBirthDate);
        Account updated = accountRepository.save(accountToUpdate);
        AccountUpdateResponse response = Mapper.toUpdateResponse(updated);
        log.info(AccountServiceLogConstants.UPDATE_ACCOUNT_FLOW_COMPLETED, accountId);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDetailResponse getByAccountId(Long accountId) {
        log.info(AccountServiceLogConstants.GET_ACCOUNT_BY_ID_FLOW_STARTED, accountId);
        apiAccessValidation.requireReadableAccount(accountId);
        Account account = getAccountOrThrow(accountId);
        List<AccountProfileTypeEnum> profileTypes = accountProfileRepository.findByAccountIdOrderByIdAsc(accountId).stream()
                .map(ap -> ap.getProfile().getProfileType())
                .toList();
        AccountDetailResponse response = Mapper.toAccountDetailResponse(account, profileTypes);
        log.info(AccountServiceLogConstants.GET_ACCOUNT_BY_ID_FLOW_COMPLETED, accountId);
        return response;
    }

    @Override
    @Transactional
    public void deactivateAccount(Long accountId) {
        log.info(AccountServiceLogConstants.DEACTIVATE_ACCOUNT_FLOW_STARTED, accountId);
        apiAccessValidation.requireOwnerStandardOrFull(accountId);
        Account account = getAccountOrThrow(accountId);
        if (account.getStatus() == AccountStatusEnum.INACTIVE) {
            log.info(AccountServiceLogConstants.DEACTIVATE_ACCOUNT_IDEMPOTENT_ALREADY_INACTIVE, accountId);
            return;
        }
        account.setStatus(AccountStatusEnum.INACTIVE);
        account.setLastUpdatedAt(LocalDateTime.now());
        Account saved = accountRepository.save(account);
        accountStatusHistoryRepository.save(Mapper.toAccountStatusHistory(saved));
        log.info(AccountServiceLogConstants.DEACTIVATE_ACCOUNT_FLOW_COMPLETED, accountId);
    }

    @Override
    @Transactional
    public void activateAccount(Long accountId) {
        log.info(AccountServiceLogConstants.ACTIVATE_ACCOUNT_FLOW_STARTED, accountId);
        apiAccessValidation.requireOwnerStandardOrFull(accountId);
        Account account = getAccountOrThrow(accountId);
        if (account.getStatus() == AccountStatusEnum.ACTIVE) {
            log.info(AccountServiceLogConstants.ACTIVATE_ACCOUNT_IDEMPOTENT_ALREADY_ACTIVE, accountId);
            return;
        }
        account.setStatus(AccountStatusEnum.ACTIVE);
        account.setLastUpdatedAt(LocalDateTime.now());
        Account saved = accountRepository.save(account);
        accountStatusHistoryRepository.save(Mapper.toAccountStatusHistory(saved));
        log.info(AccountServiceLogConstants.ACTIVATE_ACCOUNT_FLOW_COMPLETED, accountId);
    }

    @Override
    @Transactional
    public AccountProfileLinkResponse linkProfileToAccount(Long accountId, AccountProfileLinkRequest request) {
        log.info(
                AccountServiceLogConstants.LINK_PROFILE_TO_ACCOUNT_FLOW_STARTED,
                accountId,
                request.profileType()
        );
        apiAccessValidation.requireReadableAccount(accountId);
        Account account = getAccountOrThrow(accountId);
        assertAccountActiveForProfileBinding(
                account,
                () -> log.warn(AccountServiceLogConstants.LINK_PROFILE_TO_ACCOUNT_REJECTED_ACCOUNT_NOT_ACTIVE)
        );
        assertProfileTypeAllowedForLinkFlow(request.profileType());
        Profile profile = profileRepository
                .findByProfileType(request.profileType())
                .orElseThrow(() -> new IllegalStateException(ProfileCatalogMessageConstants.REQUIRED_PROFILE_MISSING_FROM_CATALOG));
        if (accountProfileRepository.existsByAccountIdAndProfileType(accountId, request.profileType())) {
            log.warn(AccountServiceLogConstants.LINK_PROFILE_TO_ACCOUNT_REJECTED_ALREADY_LINKED);
            throw new AccountException(AccountProfileLinkValidationConstants.MESSAGE_PROFILE_ALREADY_LINKED_TO_ACCOUNT);
        }
        accountProfileRepository.save(Mapper.toAccountProfile(account, profile));
        accountHistoryRepository.save(Mapper.toAccountHistory(account, profile, AccountHistoryActionEnum.LINK));
        log.info(
                AccountServiceLogConstants.LINK_PROFILE_TO_ACCOUNT_FLOW_COMPLETED,
                accountId,
                profile.getId()
        );
        return Mapper.toProfileLinkResponse(account, profile);
    }

    @Override
    @Transactional
    public void unlinkProfileFromAccount(Long accountId, AccountProfileUnlinkRequest request) {
        log.info(
                AccountServiceLogConstants.UNLINK_PROFILE_FROM_ACCOUNT_FLOW_STARTED,
                accountId,
                request.profileType()
        );
        apiAccessValidation.requireReadableAccount(accountId);
        Account account = getAccountOrThrow(accountId);
        assertAccountActiveForProfileBinding(
                account,
                () -> log.warn(AccountServiceLogConstants.UNLINK_PROFILE_FROM_ACCOUNT_REJECTED_ACCOUNT_NOT_ACTIVE)
        );
        assertProfileTypeAllowedForUnlinkFlow(request.profileType());
        Profile profile = profileRepository
                .findByProfileType(request.profileType())
                .orElseThrow(() -> new IllegalStateException(ProfileCatalogMessageConstants.REQUIRED_PROFILE_MISSING_FROM_CATALOG));
        if (accountProfileRepository.existsByAccountIdAndProfileType(accountId, request.profileType())) {
            accountProfileRepository.deleteByAccountIdAndProfileType(accountId, request.profileType());
            accountHistoryRepository.save(Mapper.toAccountHistory(account, profile, AccountHistoryActionEnum.UNLINK));
            log.info(
                    AccountServiceLogConstants.UNLINK_PROFILE_FROM_ACCOUNT_FLOW_COMPLETED,
                    accountId,
                    profile.getId()
            );
        } else {
            log.info(AccountServiceLogConstants.UNLINK_PROFILE_FROM_ACCOUNT_IDEMPOTENT_NO_BINDING);
        }
    }

    private void assertAccountActiveForProfileBinding(Account account, Runnable logNotActiveWarning) {
        if (account.getStatus() != AccountStatusEnum.ACTIVE) {
            logNotActiveWarning.run();
            throw new AccountException(AccountProfileLinkValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_PROFILE_BINDING_OPERATIONS);
        }
    }

    private void assertProfileTypeAllowedForLinkFlow(AccountProfileTypeEnum profileType) {
        if (profileType == AccountProfileTypeEnum.ANNOTATOR) {
            log.warn(AccountServiceLogConstants.LINK_PROFILE_TO_ACCOUNT_REJECTED_ANNOTATOR);
            throw new AccountException(AccountProfileLinkValidationConstants.MESSAGE_ANNOTATOR_CANNOT_BE_LINKED_VIA_THIS_FLOW);
        }
    }

    private void assertProfileTypeAllowedForUnlinkFlow(AccountProfileTypeEnum profileType) {
        if (profileType == AccountProfileTypeEnum.ANNOTATOR) {
            log.warn(AccountServiceLogConstants.UNLINK_PROFILE_FROM_ACCOUNT_REJECTED_ANNOTATOR);
            throw new AccountException(AccountProfileLinkValidationConstants.MESSAGE_ANNOTATOR_CANNOT_BE_UNLINKED);
        }
    }

    private String validateNameField(String rawValue, String requiredMessage) {
        String trimmed = rawValue == null ? null : rawValue.trim();
        if (trimmed == null || trimmed.isEmpty()) {
            throw new AccountException(requiredMessage);
        }
        if (!trimmed.matches(NAME_PATTERN_REGEX)) {
            throw new AccountException(AccountUpdateValidationConstants.MESSAGE_NAME_INVALID_FORMAT);
        }
        if (isSigla(trimmed)) {
            throw new AccountException(AccountUpdateValidationConstants.MESSAGE_NAME_CANNOT_BE_SIGLA);
        }
        return trimmed;
    }

    private boolean isSigla(String name) {
        String withoutSpaces = name.replace(" ", "");
        boolean containsSpaces = name.contains(" ");
        if (containsSpaces) {
            return false;
        }
        boolean allUppercase = withoutSpaces.equals(withoutSpaces.toUpperCase(Locale.ROOT));
        // Regra conservadora: abreviacao/sigla costuma ser tudo maiusculo e curta.
        return allUppercase && withoutSpaces.length() > 0 && withoutSpaces.length() <= MAX_SIGLA_LENGTH;
    }

    private Account getAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountException(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
    }

    private void assertAccountAllowsUpdate(Account account) {
        if (account.getStatus() == AccountStatusEnum.INACTIVE) {
            log.warn(AccountServiceLogConstants.UPDATE_ACCOUNT_REJECTED_INACTIVE_STATUS);
            throw new AccountException(AccountUpdateValidationConstants.MESSAGE_CANNOT_UPDATE_INACTIVE_ACCOUNT);
        }
    }

    private void validateUpdateAndPersist(Account accountToUpdate, LocalDate birthDate, boolean hasBirthDate) {
        if (hasBirthDate) {
            validateBirthDate(birthDate);
            accountToUpdate.setBirthDate(birthDate);
        }
    }

    private static final String NAME_PATTERN_REGEX = "^[\\p{L}]+( [\\p{L}]+)*$";
    private static final int MAX_SIGLA_LENGTH = 3;

    private Profile findRequiredAnnotatorProfile() {
        return profileRepository
                .findByProfileType(AccountProfileTypeEnum.ANNOTATOR)
                .orElseThrow(() -> new IllegalStateException(ProfileCatalogMessageConstants.REQUIRED_PROFILE_MISSING_FROM_CATALOG));
    }

    private static String buildFormattedFullName(UserCreateRequest request) {
        return FullNameFormatter.formatFromParts(request.firstName(), request.lastName());
    }

    private void validateBirthDate(LocalDate birthDate) {
        log.info(AccountServiceLogConstants.VALIDATE_BIRTH_DATE_FLOW_STARTED);
        LocalDate today = LocalDate.now();
        if (birthDate.isAfter(today)) {
            log.warn(AccountServiceLogConstants.VALIDATE_BIRTH_DATE_REJECTED_FUTURE);
            throw new AccountException(AccountRegistrationValidationConstants.MESSAGE_BIRTH_DATE_INVALID);
        }
        int ageYears = Period.between(birthDate, today).getYears();
        if (ageYears < AccountRegistrationValidationConstants.MIN_REGISTRATION_AGE_YEARS) {
            log.warn(AccountServiceLogConstants.VALIDATE_BIRTH_DATE_REJECTED_BELOW_MIN_AGE);
            throw new AccountException(AccountRegistrationValidationConstants.MESSAGE_MIN_AGE_NOT_MET);
        }
        if (ageYears > AccountRegistrationValidationConstants.MAX_REGISTRATION_AGE_YEARS) {
            log.warn(AccountServiceLogConstants.VALIDATE_BIRTH_DATE_REJECTED_ABOVE_MAX_AGE);
            throw new AccountException(AccountRegistrationValidationConstants.MESSAGE_MAX_AGE_EXCEEDED);
        }
        log.info(AccountServiceLogConstants.VALIDATE_BIRTH_DATE_FLOW_COMPLETED);
    }
}
