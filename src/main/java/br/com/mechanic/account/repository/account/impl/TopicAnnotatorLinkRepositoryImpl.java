package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.topic.TopicAnnotatorLink;

import java.util.Optional;

public interface TopicAnnotatorLinkRepositoryImpl {

    TopicAnnotatorLink save(TopicAnnotatorLink link);

    boolean existsByTopicIdAndAnnotatorAccountId(Long topicId, Long annotatorAccountId);

    Optional<TopicAnnotatorLink> findByTopicIdAndAnnotatorAccountId(Long topicId, Long annotatorAccountId);
}
