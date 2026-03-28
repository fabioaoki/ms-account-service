package br.com.mechanic.account.repository.account;

import br.com.mechanic.account.entity.account.AccountProfile;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountProfileRepository extends JpaRepository<AccountProfile, Long> {

    List<AccountProfile> findByAccount_IdOrderByIdAsc(Long accountId);

    Optional<AccountProfile> findByAccount_IdAndProfile_ProfileType(Long accountId, AccountProfileTypeEnum profileType);

    boolean existsByAccount_IdAndProfile_ProfileType(Long accountId, AccountProfileTypeEnum profileType);

    void deleteByAccount_IdAndProfile_ProfileType(Long accountId, AccountProfileTypeEnum profileType);

    long countByAccount_Id(Long accountId);
}
