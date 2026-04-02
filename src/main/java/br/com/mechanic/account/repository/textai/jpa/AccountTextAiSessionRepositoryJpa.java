package br.com.mechanic.account.repository.textai.jpa;

import br.com.mechanic.account.entity.textai.AccountTextAiSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountTextAiSessionRepositoryJpa extends JpaRepository<AccountTextAiSession, Long> {

    Optional<AccountTextAiSession> findByAccount_IdAndOpenAiThreadId(Long accountId, String openAiThreadId);
}
