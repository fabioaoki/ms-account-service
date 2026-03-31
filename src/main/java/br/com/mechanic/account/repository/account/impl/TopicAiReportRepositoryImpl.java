package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.topic.TopicAiReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TopicAiReportRepositoryImpl {

    TopicAiReport save(TopicAiReport report);

    List<TopicAiReport> findByTopicIdOrderByCreatedAtDesc(Long topicId);

    Page<TopicAiReport> findByTopicOwnerAccountIdOrderByCreatedAtDesc(Long topicOwnerAccountId, Pageable pageable);
}
