package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.topic.TopicAnnotatorLink;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import br.com.mechanic.account.repository.account.jpa.TopicAnnotatorLinkRepository;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Sort;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
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

    @Override
    public List<TopicAnnotatorLink> findAllByAnnotatorAccountIdWithOptionalFilters(
            Long annotatorAccountId,
            Long topicIdOrNull,
            Long topicOwnerAccountIdOrNull,
            TopicStatusEnum topicStatusOrNull,
            Sort sort
    ) {
        boolean hasTopic = topicIdOrNull != null;
        boolean hasOwner = topicOwnerAccountIdOrNull != null;
        boolean hasStatus = topicStatusOrNull != null;

        if (hasTopic && hasOwner && hasStatus) {
            return repository.findAllByAnnotatorAccount_IdAndTopic_IdAndTopicOwnerAccount_IdAndTopic_Status(
                    annotatorAccountId,
                    topicIdOrNull,
                    topicOwnerAccountIdOrNull,
                    topicStatusOrNull,
                    sort
            );
        }
        if (hasTopic && hasOwner) {
            return repository.findAllByAnnotatorAccount_IdAndTopic_IdAndTopicOwnerAccount_Id(
                    annotatorAccountId,
                    topicIdOrNull,
                    topicOwnerAccountIdOrNull,
                    sort
            );
        }
        if (hasTopic && hasStatus) {
            return repository.findAllByAnnotatorAccount_IdAndTopic_IdAndTopic_Status(
                    annotatorAccountId,
                    topicIdOrNull,
                    topicStatusOrNull,
                    sort
            );
        }
        if (hasOwner && hasStatus) {
            return repository.findAllByAnnotatorAccount_IdAndTopicOwnerAccount_IdAndTopic_Status(
                    annotatorAccountId,
                    topicOwnerAccountIdOrNull,
                    topicStatusOrNull,
                    sort
            );
        }
        if (hasTopic) {
            return repository.findAllByAnnotatorAccount_IdAndTopic_Id(annotatorAccountId, topicIdOrNull, sort);
        }
        if (hasOwner) {
            return repository.findAllByAnnotatorAccount_IdAndTopicOwnerAccount_Id(
                    annotatorAccountId,
                    topicOwnerAccountIdOrNull,
                    sort
            );
        }
        if (hasStatus) {
            return repository.findAllByAnnotatorAccount_IdAndTopic_Status(
                    annotatorAccountId,
                    topicStatusOrNull,
                    sort
            );
        }
        return repository.findAllByAnnotatorAccount_Id(annotatorAccountId, sort);
    }
}
