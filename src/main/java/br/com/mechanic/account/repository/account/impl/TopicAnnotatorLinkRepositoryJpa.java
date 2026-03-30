package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.topic.TopicAnnotatorLink;
import br.com.mechanic.account.repository.account.jpa.TopicAnnotatorLinkRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class TopicAnnotatorLinkRepositoryJpa implements TopicAnnotatorLinkRepositoryImpl {

    private final TopicAnnotatorLinkRepository repository;
    private final Clock clock;

    public TopicAnnotatorLinkRepositoryJpa(TopicAnnotatorLinkRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public TopicAnnotatorLink save(TopicAnnotatorLink link) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (link.getId() == null && link.getCreatedAt() == null) {
            link.setCreatedAt(now);
        }
        if (link.getId() != null) {
            link.setLastUpdatedAt(now);
        }
        return repository.save(link);
    }

    @Override
    public boolean existsByTopicIdAndAnnotatorAccountId(Long topicId, Long annotatorAccountId) {
        return repository.existsByTopic_IdAndAnnotatorAccount_Id(topicId, annotatorAccountId);
    }

    @Override
    public Optional<TopicAnnotatorLink> findByTopicIdAndAnnotatorAccountId(Long topicId, Long annotatorAccountId) {
        return repository.findByTopic_IdAndAnnotatorAccount_Id(topicId, annotatorAccountId);
    }
}
