package br.com.mechanic.account.repository.account;

import br.com.mechanic.account.entity.account.AccountProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountProfileRepository extends JpaRepository<AccountProfile, Long> {
}
