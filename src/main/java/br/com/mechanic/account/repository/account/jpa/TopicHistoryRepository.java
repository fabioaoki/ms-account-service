package br.com.mechanic.account.repository.account.jpa;

import br.com.mechanic.account.entity.topic.TopicHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicHistoryRepository extends JpaRepository<TopicHistory, Long> {

    long countByTopic_Id(Long topicId);

    List<TopicHistory> findByTopic_IdOrderByIdAsc(Long topicId);
}
