package br.com.mechanic.account.repository.account.impl;

import br.com.mechanic.account.entity.topic.TopicAiReport;
import br.com.mechanic.account.repository.account.jpa.TopicAiReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TopicAiReportRepositoryJpa implements TopicAiReportRepositoryImpl {

    private final TopicAiReportRepository repository;
    private final Clock clock;

    public TopicAiReportRepositoryJpa(TopicAiReportRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public TopicAiReport save(TopicAiReport report) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (report.getId() == null) {
            if (report.getCreatedAt() == null) {
                report.setCreatedAt(now);
            }
            report.setLastUpdatedAt(now);
        } else {
            report.setLastUpdatedAt(now);
        }
        return repository.save(report);
    }

    @Override
    public List<TopicAiReport> findByTopicIdOrderByCreatedAtDesc(Long topicId) {
        return repository.findByTopic_IdOrderByCreatedAtDesc(topicId);
    }

    @Override
    public Page<TopicAiReport> findByTopicOwnerAccountIdOrderByCreatedAtDesc(
            Long topicOwnerAccountId,
            Pageable pageable
    ) {
        return repository.findByTopicOwnerAccount_IdOrderByCreatedAtDesc(topicOwnerAccountId, pageable);
    }
}
