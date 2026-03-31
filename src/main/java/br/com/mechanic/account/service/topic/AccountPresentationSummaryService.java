package br.com.mechanic.account.service.topic;

import br.com.mechanic.account.constant.AccountPresentationSummaryValidationConstants;
import br.com.mechanic.account.constant.AccountUpdateValidationConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.account.AccountPresentationSummary;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import br.com.mechanic.account.exception.AccountException;
import br.com.mechanic.account.repository.account.impl.AccountPresentationSummaryRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.AccountProfileRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.AccountRepositoryImpl;
import br.com.mechanic.account.service.request.AccountPresentationSummaryUpsertRequest;
import br.com.mechanic.account.service.response.AccountPresentationSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountPresentationSummaryService implements AccountPresentationSummaryServiceBO {

    private final AccountRepositoryImpl accountRepository;
    private final AccountProfileRepositoryImpl accountProfileRepository;
    private final AccountPresentationSummaryRepositoryImpl accountPresentationSummaryRepository;
    private final Clock clock;

    @Override
    @Transactional
    public AccountPresentationSummaryResponse createSummary(Long accountId, AccountPresentationSummaryUpsertRequest request) {
        Account account = getAccountOrThrowAndAssertActiveForTopicEndpoints(accountId);
        assertAccountHasMoreThanOneProfileType(accountId);
        if (accountPresentationSummaryRepository.existsByAccountId(accountId)) {
            throw new AccountException(
                    AccountPresentationSummaryValidationConstants.MESSAGE_ACCOUNT_PRESENTATION_SUMMARY_ALREADY_EXISTS
            );
        }
        String normalizedSummary = normalizeAndValidateSummary(request.summary());
        AccountPresentationSummary saved = accountPresentationSummaryRepository.save(
                AccountPresentationSummary.builder()
                        .account(account)
                        .summary(normalizedSummary)
                        .createdAt(LocalDateTime.now(clock))
                        .lastUpdatedAt(null)
                        .build()
        );
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AccountPresentationSummaryResponse updateSummary(Long accountId, AccountPresentationSummaryUpsertRequest request) {
        getAccountOrThrowAndAssertActiveForTopicEndpoints(accountId);
        assertAccountHasMoreThanOneProfileType(accountId);
        AccountPresentationSummary existing = accountPresentationSummaryRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AccountException(
                        AccountPresentationSummaryValidationConstants.MESSAGE_ACCOUNT_PRESENTATION_SUMMARY_NOT_FOUND
                ));
        existing.setSummary(normalizeAndValidateSummary(request.summary()));
        existing.setLastUpdatedAt(LocalDateTime.now(clock));
        AccountPresentationSummary updated = accountPresentationSummaryRepository.save(existing);
        return toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountPresentationSummaryResponse getSummary(Long accountId) {
        getAccountOrThrowAndAssertActiveForTopicEndpoints(accountId);
        AccountPresentationSummary existing = accountPresentationSummaryRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AccountException(
                        AccountPresentationSummaryValidationConstants.MESSAGE_ACCOUNT_PRESENTATION_SUMMARY_NOT_FOUND
                ));
        return toResponse(existing);
    }

    private void assertAccountHasMoreThanOneProfileType(Long accountId) {
        int profileTypeCount = accountProfileRepository.findByAccountIdOrderByIdAsc(accountId).size();
        if (profileTypeCount <= 1) {
            throw new AccountException(
                    AccountPresentationSummaryValidationConstants.MESSAGE_ACCOUNT_MUST_HAVE_MORE_THAN_ONE_PROFILE_TYPE
            );
        }
    }

    private Account getAccountOrThrowAndAssertActiveForTopicEndpoints(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountException(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
        if (account.getStatus() != AccountStatusEnum.ACTIVE) {
            throw new AccountException(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS);
        }
        return account;
    }

    private static String normalizeAndValidateSummary(String rawSummary) {
        String trimmed = rawSummary == null ? null : rawSummary.trim();
        if (trimmed == null || trimmed.isEmpty()) {
            throw new AccountException(AccountPresentationSummaryValidationConstants.MESSAGE_SUMMARY_REQUIRED);
        }
        if (trimmed.length() < AccountPresentationSummaryValidationConstants.MIN_SUMMARY_CHAR_COUNT_AFTER_TRIM) {
            throw new AccountException(
                    AccountPresentationSummaryValidationConstants.MESSAGE_SUMMARY_INVALID_LENGTH.formatted(
                            AccountPresentationSummaryValidationConstants.MIN_SUMMARY_CHAR_COUNT_AFTER_TRIM
                    )
            );
        }
        if (trimmed.length() > AccountPresentationSummaryValidationConstants.MAX_SUMMARY_CHAR_COUNT) {
            throw new AccountException(
                    AccountPresentationSummaryValidationConstants.MESSAGE_SUMMARY_EXCEEDS_MAX_LENGTH.formatted(
                            AccountPresentationSummaryValidationConstants.MAX_SUMMARY_CHAR_COUNT
                    )
            );
        }
        return trimmed;
    }

    private static AccountPresentationSummaryResponse toResponse(AccountPresentationSummary summary) {
        return new AccountPresentationSummaryResponse(
                summary.getAccount().getId(),
                summary.getSummary(),
                summary.getCreatedAt(),
                summary.getLastUpdatedAt()
        );
    }
}
