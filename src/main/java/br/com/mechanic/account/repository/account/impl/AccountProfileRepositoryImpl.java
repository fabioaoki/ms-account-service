package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.account.AccountProfile;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;

import java.util.List;
import java.util.Optional;

public interface AccountProfileRepositoryImpl {

    AccountProfile save(AccountProfile accountProfile);

    Optional<AccountProfile> findByAccountIdAndProfileType(Long accountId, AccountProfileTypeEnum profileType);

    boolean existsByAccountIdAndProfileType(Long accountId, AccountProfileTypeEnum profileType);

    void deleteByAccountIdAndProfileType(Long accountId, AccountProfileTypeEnum profileType);

    List<AccountProfile> findByAccountIdOrderByIdAsc(Long accountId);
}