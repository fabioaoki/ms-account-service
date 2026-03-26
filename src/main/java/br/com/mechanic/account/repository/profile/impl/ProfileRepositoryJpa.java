package br.com.mechanic.account.repository.profile.impl;

import br.com.mechanic.account.entity.profile.Profile;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.repository.profile.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProfileRepositoryJpa implements ProfileRepositoryImpl {

   private final ProfileRepository repository;

   @Autowired
    public ProfileRepositoryJpa(ProfileRepository repository) {
        this.repository = repository;
    }


    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public Profile save(Profile profile) {
        return repository.save(profile);
    }

    @Override
    public Optional<Profile> findByProfileType(AccountProfileTypeEnum profileType) {
        return repository.findByProfileType(profileType);
    }
}
