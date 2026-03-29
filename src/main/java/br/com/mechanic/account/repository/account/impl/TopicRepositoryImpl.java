package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.topic.Topic;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TopicRepositoryImpl {

    Topic save(Topic topic);

    Optional<Topic> findByIdAndAccountId(Long topicId, Long accountId);

    Page<Topic> findAllByAccountIdWithOptionalFilters(
            Long accountId,
            TopicStatusEnum statusFilter,
            AccountProfileTypeEnum profileTypeFilter,
            Pageable pageable
    );
}
