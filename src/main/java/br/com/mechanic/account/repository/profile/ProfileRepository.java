package br.com.mechanic.account.repository.profile;

import br.com.mechanic.account.entity.profile.Profile;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByProfileType(AccountProfileTypeEnum profileType);
}
