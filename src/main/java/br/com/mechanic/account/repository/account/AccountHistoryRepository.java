package br.com.mechanic.account.repository.account;

import br.com.mechanic.account.entity.account.AccountHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountHistoryRepository extends JpaRepository<AccountHistory, Long> {

    long countByAccount_Id(Long accountId);
}
