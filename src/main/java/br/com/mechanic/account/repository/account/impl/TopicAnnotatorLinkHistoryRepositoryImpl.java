package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.topic.TopicAnnotatorLinkHistory;

public interface TopicAnnotatorLinkHistoryRepositoryImpl {

    TopicAnnotatorLinkHistory save(TopicAnnotatorLinkHistory history);

    long countByLinkId(Long linkId);
}
