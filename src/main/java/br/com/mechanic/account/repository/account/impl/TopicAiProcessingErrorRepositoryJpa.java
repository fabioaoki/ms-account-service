package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.topic.TopicAiProcessingError;
import br.com.mechanic.account.repository.account.jpa.TopicAiProcessingErrorRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class TopicAiProcessingErrorRepositoryJpa implements TopicAiProcessingErrorRepositoryImpl {

    private final TopicAiProcessingErrorRepository repository;
    private final Clock clock;

    public TopicAiProcessingErrorRepositoryJpa(TopicAiProcessingErrorRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public TopicAiProcessingError save(TopicAiProcessingError error) {
        if (error.getCreatedAt() == null) {
            error.setCreatedAt(LocalDateTime.now(clock));
        }
        return repository.save(error);
    }
}
