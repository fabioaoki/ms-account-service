package br.com.mechanic.account.repository.account.jpa;

import br.com.mechanic.account.entity.account.AccountAnnotatorBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountAnnotatorBlockRepository extends JpaRepository<AccountAnnotatorBlock, Long> {

    boolean existsByBlockerAccount_IdAndBlockedAccount_Id(Long blockerAccountId, Long blockedAccountId);

    Page<AccountAnnotatorBlock> findAllByBlockerAccount_Id(Long blockerAccountId, Pageable pageable);
}
