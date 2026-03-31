package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.account.AccountAnnotatorBlock;
import br.com.mechanic.account.repository.account.jpa.AccountAnnotatorBlockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
@Component
public class AccountAnnotatorBlockRepositoryJpa implements AccountAnnotatorBlockRepositoryImpl {

    private final AccountAnnotatorBlockRepository repository;
    private final Clock clock;

    public AccountAnnotatorBlockRepositoryJpa(AccountAnnotatorBlockRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public AccountAnnotatorBlock save(AccountAnnotatorBlock block) {
        if (block.getCreatedAt() == null) {
            block.setCreatedAt(LocalDateTime.now(clock));
        }
        return repository.save(block);
    }

    @Override
    public boolean existsByBlockerAccountIdAndBlockedAccountId(Long blockerAccountId, Long blockedAccountId) {
        return repository.existsByBlockerAccount_IdAndBlockedAccount_Id(blockerAccountId, blockedAccountId);
    }

    @Override
    public Page<AccountAnnotatorBlock> findAllByBlockerAccountId(Long blockerAccountId, Pageable pageable) {
        return repository.findAllByBlockerAccount_Id(blockerAccountId, pageable);
    }
}
