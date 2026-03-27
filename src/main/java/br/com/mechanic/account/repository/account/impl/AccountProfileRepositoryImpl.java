package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.account.AccountProfile;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;

public interface AccountProfileRepositoryImpl {

    AccountProfile save(AccountProfile accountProfile);

    boolean existsByAccountIdAndProfileType(Long accountId, AccountProfileTypeEnum profileType);

    void deleteByAccountIdAndProfileType(Long accountId, AccountProfileTypeEnum profileType);
}