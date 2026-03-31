package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.account.AccountPresentationSummary;
import br.com.mechanic.account.repository.account.jpa.AccountPresentationSummaryRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AccountPresentationSummaryRepositoryJpa implements AccountPresentationSummaryRepositoryImpl {

    private final AccountPresentationSummaryRepository repository;
    private final Clock clock;

    public AccountPresentationSummaryRepositoryJpa(AccountPresentationSummaryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public AccountPresentationSummary save(AccountPresentationSummary summary) {
        if (summary.getCreatedAt() == null) {
            summary.setCreatedAt(LocalDateTime.now(clock));
        }
        return repository.save(summary);
    }

    @Override
    public Optional<AccountPresentationSummary> findByAccountId(Long accountId) {
        return repository.findByAccount_Id(accountId);
    }

    @Override
    public boolean existsByAccountId(Long accountId) {
        return repository.existsByAccount_Id(accountId);
    }
}
