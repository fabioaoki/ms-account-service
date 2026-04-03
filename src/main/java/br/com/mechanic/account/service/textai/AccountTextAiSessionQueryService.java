package br.com.mechanic.account.service.textai;

import br.com.mechanic.account.constant.AccountUpdateValidationConstants;
import br.com.mechanic.account.constant.TextAiAssistantValidationConstants;
import br.com.mechanic.account.constant.TopicPaginationConstants;
import br.com.mechanic.account.constant.TopicServiceLogConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.textai.AccountTextAiSession;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import br.com.mechanic.account.exception.AccountException;
import br.com.mechanic.account.repository.account.jpa.AccountRepositoryJpa;
import br.com.mechanic.account.repository.textai.jpa.AccountTextAiSessionRepositoryJpa;
import br.com.mechanic.account.security.ApiAccessValidation;
import br.com.mechanic.account.service.response.AccountTextAiSessionPageResponse;
import br.com.mechanic.account.service.response.AccountTextAiSessionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountTextAiSessionQueryService implements AccountTextAiSessionQueryServiceBO {

    private final ApiAccessValidation apiAccessValidation;
    private final AccountRepositoryJpa accountRepositoryJpa;
    private final AccountTextAiSessionRepositoryJpa sessionRepositoryJpa;

    @Override
    @Transactional(readOnly = true)
    public AccountTextAiSessionPageResponse listByAccountId(Long accountId, Integer page, Integer size) {
        apiAccessValidation.requireTextAiAssistantAccess(accountId);
        assertAccountActiveForTextAi(accountId);
        ResolvedPaginationParams params = resolvePagination(page, size);
        Pageable pageable = PageRequest.of(
                params.pageNumber(),
                params.pageSize(),
                Sort.by(Sort.Direction.DESC, TopicPaginationConstants.SORT_PROPERTY_CREATED_AT)
                        .and(Sort.by(Sort.Direction.DESC, TopicPaginationConstants.SORT_PROPERTY_ID))
        );
        Page<AccountTextAiSession> pageResult = sessionRepositoryJpa.findAllByAccount_IdAndIsDeletedIsNull(
                accountId,
                pageable
        );
        List<AccountTextAiSessionResponse> content = pageResult.getContent().stream()
                .map(AccountTextAiSessionMapper::toResponse)
                .toList();
        return new AccountTextAiSessionPageResponse(
                content,
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getSize(),
                pageResult.getNumber(),
                pageResult.isFirst(),
                pageResult.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AccountTextAiSessionResponse getByIdAndAccountId(Long accountId, Long textAiSessionId) {
        apiAccessValidation.requireTextAiAssistantAccess(accountId);
        assertAccountActiveForTextAi(accountId);
        AccountTextAiSession session = sessionRepositoryJpa
                .findByIdAndAccount_IdAndIsDeletedIsNull(textAiSessionId, accountId)
                .orElseThrow(() -> new AccountException(TextAiAssistantValidationConstants.MESSAGE_TEXT_AI_SESSION_NOT_FOUND_FOR_ACCOUNT));
        return AccountTextAiSessionMapper.toResponse(session);
    }

    private void assertAccountActiveForTextAi(Long accountId) {
        Account account = accountRepositoryJpa.findById(accountId)
                .orElseThrow(() -> new AccountException(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
        if (account.getStatus() != AccountStatusEnum.ACTIVE) {
            log.warn(TopicServiceLogConstants.TOPIC_ENDPOINT_REJECTED_ACCOUNT_NOT_ACTIVE);
            throw new AccountException(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS);
        }
    }

    private static ResolvedPaginationParams resolvePagination(Integer page, Integer size) {
        int pageNumber = page == null ? TopicPaginationConstants.DEFAULT_PAGE_NUMBER : page;
        int pageSize = size == null ? TopicPaginationConstants.DEFAULT_PAGE_SIZE : size;
        if (pageNumber < TopicPaginationConstants.MIN_PAGE_NUMBER) {
            throw new AccountException(TextAiAssistantValidationConstants.MESSAGE_TEXT_AI_SESSION_PAGE_NUMBER_NEGATIVE);
        }
        if (pageSize < TopicPaginationConstants.MIN_PAGE_SIZE || pageSize > TopicPaginationConstants.MAX_PAGE_SIZE) {
            throw new AccountException(
                    TextAiAssistantValidationConstants.MESSAGE_TEXT_AI_SESSION_PAGE_SIZE_INVALID.formatted(
                            TopicPaginationConstants.MIN_PAGE_SIZE,
                            TopicPaginationConstants.MAX_PAGE_SIZE
                    )
            );
        }
        return new ResolvedPaginationParams(pageNumber, pageSize);
    }

    private record ResolvedPaginationParams(int pageNumber, int pageSize) {
    }
}
