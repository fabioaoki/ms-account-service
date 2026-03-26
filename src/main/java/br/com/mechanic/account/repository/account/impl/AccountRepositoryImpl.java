package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.account.Account;

import java.util.Optional;

public interface AccountRepositoryImpl {

    Boolean existsByEmailIgnoreCase(String email);

    Optional<Account> findById(Long accountId);

    Account save(Account account);
}
