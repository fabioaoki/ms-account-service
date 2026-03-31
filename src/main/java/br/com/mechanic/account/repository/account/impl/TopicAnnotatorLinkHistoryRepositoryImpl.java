package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.topic.TopicAnnotatorLinkHistory;

import java.util.List;

public interface TopicAnnotatorLinkHistoryRepositoryImpl {

    TopicAnnotatorLinkHistory save(TopicAnnotatorLinkHistory history);

    long countByLinkId(Long linkId);

    List<TopicAnnotatorLinkHistory> findAllWithNonBlankResumeByTopicIdOrderByCreatedAtAsc(Long topicId);
}
