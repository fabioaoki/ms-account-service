package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.account.AccountStatusHistory;
import br.com.mechanic.account.repository.account.AccountStatusHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AccountStatusHistoryRepositoryJpa implements AccountStatusHistoryRepositoryImpl {

    private final AccountStatusHistoryRepository repository;

    @Autowired
    public AccountStatusHistoryRepositoryJpa(AccountStatusHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountStatusHistory save(AccountStatusHistory accountStatusHistory) {
        accountStatusHistory.setOccurredAt(LocalDateTime.now());
        return repository.save(accountStatusHistory);
    }
}
