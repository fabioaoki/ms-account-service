package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.account.AccountPresentationSummary;

import java.util.Optional;

public interface AccountPresentationSummaryRepositoryImpl {

    AccountPresentationSummary save(AccountPresentationSummary summary);

    Optional<AccountPresentationSummary> findByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);
}
