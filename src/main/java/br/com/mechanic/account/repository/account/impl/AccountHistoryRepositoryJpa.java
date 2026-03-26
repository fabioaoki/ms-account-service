package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.account.AccountHistory;
import br.com.mechanic.account.repository.account.AccountHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AccountHistoryRepositoryJpa implements AccountHistoryRepositoryImpl {

    private final AccountHistoryRepository repository;

    @Autowired
    public AccountHistoryRepositoryJpa(AccountHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountHistory save(AccountHistory accountHistory) {
        accountHistory.setCreatedAt(LocalDateTime.now());
        return repository.save(accountHistory);
    }
}
