package br.com.mechanic.account.repository.account.jpa;

import br.com.mechanic.account.entity.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepositoryJpa extends JpaRepository<Account, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<Account> findByEmailIgnoreCase(String email);
}
