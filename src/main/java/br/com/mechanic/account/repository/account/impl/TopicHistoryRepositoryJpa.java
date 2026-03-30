package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.topic.TopicHistory;
import br.com.mechanic.account.repository.account.jpa.TopicHistoryRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class TopicHistoryRepositoryJpa implements TopicHistoryRepositoryImpl {

    private final TopicHistoryRepository repository;
    private final Clock clock;

    public TopicHistoryRepositoryJpa(TopicHistoryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public TopicHistory save(TopicHistory topicHistory) {
        if (topicHistory.getCreatedAt() == null) {
            topicHistory.setCreatedAt(LocalDateTime.now(clock));
        }
        return repository.save(topicHistory);
    }
}
