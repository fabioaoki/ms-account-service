package br.com.mechanic.account.repository.account.jpa;

import br.com.mechanic.account.entity.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepositoryJpa extends JpaRepository<Account, Long> {

    boolean existsByEmailIgnoreCase(String email);
}
