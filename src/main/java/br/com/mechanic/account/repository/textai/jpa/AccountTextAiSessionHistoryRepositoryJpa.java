package br.com.mechanic.account.repository.textai.jpa;

import br.com.mechanic.account.entity.textai.AccountTextAiSessionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountTextAiSessionHistoryRepositoryJpa extends JpaRepository<AccountTextAiSessionHistory, Long> {
}
