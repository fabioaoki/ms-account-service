package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.topic.TopicAiReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TopicAiReportRepositoryImpl {

    TopicAiReport save(TopicAiReport report);

    List<TopicAiReport> findByTopicIdOrderByCreatedAtDesc(Long topicId);

    Page<TopicAiReport> findByTopicOwnerAccountIdOrderByCreatedAtDesc(Long topicOwnerAccountId, Pageable pageable);

    Optional<TopicAiReport> findLatestByTopicIdAndTopicOwnerAccountId(Long topicId, Long topicOwnerAccountId);
}
