package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.account.AccountProfile;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.repository.account.AccountProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AccountProfileRepositoryJpa implements AccountProfileRepositoryImpl {

    private final AccountProfileRepository repository;

    @Autowired
    public AccountProfileRepositoryJpa(AccountProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountProfile save(AccountProfile accountProfile) {
        accountProfile.setCreatedAt(LocalDateTime.now());
        return repository.save(accountProfile);
    }

    @Override
    public boolean existsByAccountIdAndProfileType(Long accountId, AccountProfileTypeEnum profileType) {
        return repository.existsByAccount_IdAndProfile_ProfileType(accountId, profileType);
    }

    @Override
    public void deleteByAccountIdAndProfileType(Long accountId, AccountProfileTypeEnum profileType) {
        repository.deleteByAccount_IdAndProfile_ProfileType(accountId, profileType);
    }
}
