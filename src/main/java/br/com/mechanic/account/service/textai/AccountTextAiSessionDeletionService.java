package br.com.mechanic.account.service.textai;

import br.com.mechanic.account.constant.AccountUpdateValidationConstants;
import br.com.mechanic.account.constant.TextAiAssistantValidationConstants;
import br.com.mechanic.account.constant.TopicServiceLogConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.textai.AccountTextAiSession;
import br.com.mechanic.account.entity.textai.AccountTextAiSessionHistory;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import br.com.mechanic.account.exception.AccountException;
import br.com.mechanic.account.repository.account.jpa.AccountRepositoryJpa;
import br.com.mechanic.account.repository.textai.jpa.AccountTextAiSessionHistoryRepositoryJpa;
import br.com.mechanic.account.repository.textai.jpa.AccountTextAiSessionRepositoryJpa;
import br.com.mechanic.account.security.ApiAccessValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountTextAiSessionDeletionService implements AccountTextAiSessionDeletionServiceBO {

    private final ApiAccessValidation apiAccessValidation;
    private final AccountRepositoryJpa accountRepositoryJpa;
    private final AccountTextAiSessionRepositoryJpa sessionRepositoryJpa;
    private final AccountTextAiSessionHistoryRepositoryJpa sessionHistoryRepositoryJpa;
    private final Clock clock;

    @Override
    @Transactional
    public void softDeleteByAccountIdAndSessionId(Long accountId, Long textAiSessionId) {
        apiAccessValidation.requireTextAiAssistantAccess(accountId);
        Account account = assertAccountActiveForTextAi(accountId);
        AccountTextAiSession session = sessionRepositoryJpa
                .findByIdAndAccount_Id(textAiSessionId, accountId)
                .orElseThrow(() -> new AccountException(TextAiAssistantValidationConstants.MESSAGE_TEXT_AI_SESSION_NOT_FOUND_FOR_ACCOUNT));
        if (Boolean.TRUE.equals(session.getIsDeleted())) {
            throw new AccountException(TextAiAssistantValidationConstants.MESSAGE_TEXT_AI_SESSION_NOT_FOUND_FOR_ACCOUNT);
        }
        LocalDateTime now = LocalDateTime.now(clock);
        session.setIsDeleted(Boolean.TRUE);
        session.setLastUpdatedAt(now);
        sessionRepositoryJpa.save(session);
        AccountTextAiSessionHistory historyRow = AccountTextAiSessionHistory.builder()
                .textAiSession(session)
                .account(account)
                .createdAt(now)
                .build();
        sessionHistoryRepositoryJpa.save(historyRow);
    }

    private Account assertAccountActiveForTextAi(Long accountId) {
        Account account = accountRepositoryJpa.findById(accountId)
                .orElseThrow(() -> new AccountException(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
        if (account.getStatus() != AccountStatusEnum.ACTIVE) {
            log.warn(TopicServiceLogConstants.TOPIC_ENDPOINT_REJECTED_ACCOUNT_NOT_ACTIVE);
            throw new AccountException(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS);
        }
        return account;
    }
}
