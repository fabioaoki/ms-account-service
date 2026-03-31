package br.com.mechanic.account.repository.account.jpa;

import br.com.mechanic.account.entity.topic.TopicAnnotatorLinkHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TopicAnnotatorLinkHistoryRepository extends JpaRepository<TopicAnnotatorLinkHistory, Long> {

    long countByLink_Id(Long linkId);

    @Query("""
            SELECT h FROM TopicAnnotatorLinkHistory h
            JOIN FETCH h.link l
            JOIN FETCH l.topic
            JOIN FETCH h.annotatorAccount
            WHERE l.topic.id = :topicId
              AND h.resume IS NOT NULL
            ORDER BY h.createdAt ASC
            """)
    List<TopicAnnotatorLinkHistory> findAllWithResumeNotNullByTopicIdOrderByCreatedAtAsc(@Param("topicId") Long topicId);
}
