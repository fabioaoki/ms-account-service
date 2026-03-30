package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.topic.TopicAnnotatorLinkHistory;
import br.com.mechanic.account.repository.account.jpa.TopicAnnotatorLinkHistoryRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class TopicAnnotatorLinkHistoryRepositoryJpa implements TopicAnnotatorLinkHistoryRepositoryImpl {

    private final TopicAnnotatorLinkHistoryRepository repository;
    private final Clock clock;

    public TopicAnnotatorLinkHistoryRepositoryJpa(TopicAnnotatorLinkHistoryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public TopicAnnotatorLinkHistory save(TopicAnnotatorLinkHistory history) {
        if (history.getCreatedAt() == null) {
            history.setCreatedAt(LocalDateTime.now(clock));
        }
        return repository.save(history);
    }

    @Override
    public long countByLinkId(Long linkId) {
        return repository.countByLink_Id(linkId);
    }
}
