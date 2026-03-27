package br.com.mechanic.account.repository.account;

import br.com.mechanic.account.entity.account.AccountStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountStatusHistoryRepository extends JpaRepository<AccountStatusHistory, Long> {

    long countByAccount_Id(Long accountId);
}
