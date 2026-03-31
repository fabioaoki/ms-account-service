package br.com.mechanic.account.repository.account.jpa;

import br.com.mechanic.account.entity.topic.TopicAiReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TopicAiReportRepository extends JpaRepository<TopicAiReport, Long> {

    List<TopicAiReport> findByTopic_IdOrderByCreatedAtDesc(Long topicId);

    Page<TopicAiReport> findByTopicOwnerAccount_IdOrderByCreatedAtDesc(Long topicOwnerAccountId, Pageable pageable);

    Optional<TopicAiReport> findTopByTopic_IdAndTopicOwnerAccount_IdOrderByCreatedAtDesc(
            Long topicId,
            Long topicOwnerAccountId
    );
}
