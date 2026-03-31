package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.account.AccountAnnotatorBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountAnnotatorBlockRepositoryImpl {

    AccountAnnotatorBlock save(AccountAnnotatorBlock block);

    boolean existsByBlockerAccountIdAndBlockedAccountId(Long blockerAccountId, Long blockedAccountId);

    Page<AccountAnnotatorBlock> findAllByBlockerAccountId(Long blockerAccountId, Pageable pageable);
}
