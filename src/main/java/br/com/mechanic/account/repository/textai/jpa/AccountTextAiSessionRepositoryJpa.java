package br.com.mechanic.account.repository.textai.jpa;

import br.com.mechanic.account.entity.textai.AccountTextAiSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountTextAiSessionRepositoryJpa extends JpaRepository<AccountTextAiSession, Long> {

    Optional<AccountTextAiSession> findByAccount_IdAndOpenAiThreadId(Long accountId, String openAiThreadId);

    Optional<AccountTextAiSession> findByAccount_IdAndOpenAiThreadIdAndIsDeletedIsNull(
            Long accountId,
            String openAiThreadId
    );

    @EntityGraph(attributePaths = "account")
    Page<AccountTextAiSession> findAllByAccount_IdAndIsDeletedIsNull(Long accountId, Pageable pageable);

    @EntityGraph(attributePaths = "account")
    Optional<AccountTextAiSession> findByIdAndAccount_IdAndIsDeletedIsNull(Long id, Long accountId);

    Optional<AccountTextAiSession> findByIdAndAccount_Id(Long id, Long accountId);
}
