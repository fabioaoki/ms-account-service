package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.repository.account.jpa.AccountRepositoryJpa;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AccountRepositoryAdapter implements AccountRepositoryImpl {

    private final AccountRepositoryJpa jpaRepository;

    public AccountRepositoryAdapter(AccountRepositoryJpa jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Boolean existsByEmailIgnoreCase(String email) {
        return jpaRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public Account save(Account entity) {
        LocalDateTime now = LocalDateTime.now();
        if (entity.getId() == null) {
            entity.setCreatedAt(now);
        }
        entity.setLastUpdatedAt(now);
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<Account> findById(Long accountId) {
        return jpaRepository.findById(accountId);
    }
}
