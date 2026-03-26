package br.com.mechanic.account.repository.profile.impl;

import br.com.mechanic.account.entity.profile.Profile;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;

import java.util.Optional;

public interface ProfileRepositoryImpl {

    long count();

    Profile save(Profile profile);

    Optional<Profile> findByProfileType(AccountProfileTypeEnum profileType);
}
