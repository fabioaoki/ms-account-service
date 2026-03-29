package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.topic.Topic;

import java.util.Optional;

public interface TopicRepositoryImpl {

    Topic save(Topic topic);

    Optional<Topic> findByIdAndAccountId(Long topicId, Long accountId);
}
