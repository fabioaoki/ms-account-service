package br.com.mechanic.account.repository.account.jpa;

import br.com.mechanic.account.entity.account.AccountPresentationSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountPresentationSummaryRepository extends JpaRepository<AccountPresentationSummary, Long> {

    Optional<AccountPresentationSummary> findByAccount_Id(Long accountId);

    boolean existsByAccount_Id(Long accountId);
}
