package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.topic.Topic;
import br.com.mechanic.account.repository.account.jpa.TopicRepositoryJpa;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class TopicRepositoryAdapter implements TopicRepositoryImpl {

    private final TopicRepositoryJpa jpaRepository;

    public TopicRepositoryAdapter(TopicRepositoryJpa jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Topic> findByIdAndAccountId(Long topicId, Long accountId) {
        return jpaRepository.findByIdAndAccount_Id(topicId, accountId);
    }

    @Override
    public Topic save(Topic entity) {
        LocalDateTime now = LocalDateTime.now();
        if (entity.getId() == null && entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        if (entity.getId() != null) {
            entity.setLastUpdatedAt(now);
        }
        return jpaRepository.save(entity);
    }
}
