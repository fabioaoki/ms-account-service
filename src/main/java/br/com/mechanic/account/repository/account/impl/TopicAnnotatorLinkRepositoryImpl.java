package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.topic.TopicAnnotatorLink;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TopicAnnotatorLinkRepositoryImpl {

    TopicAnnotatorLink save(TopicAnnotatorLink link);

    boolean existsByTopicIdAndAnnotatorAccountId(Long topicId, Long annotatorAccountId);

    boolean existsByTopicOwnerAccountIdAndAnnotatorAccountId(Long topicOwnerAccountId, Long annotatorAccountId);

    Optional<TopicAnnotatorLink> findByTopicIdAndAnnotatorAccountId(Long topicId, Long annotatorAccountId);

    List<TopicAnnotatorLink> findAllByTopicIdWithAnnotatorAccountOrderByCreatedAtAsc(Long topicId);

    List<TopicAnnotatorLink> findAllByTopicIdInWithAnnotatorAccountOrdered(Collection<Long> topicIds);

    List<TopicAnnotatorLink> findAllByAnnotatorAccountIdWithOptionalFilters(
            Long annotatorAccountId,
            Long topicIdOrNull,
            Long topicOwnerAccountIdOrNull,
            TopicStatusEnum topicStatusOrNull,
            Sort sort
    );
}
