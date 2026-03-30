package br.com.mechanic.account.repository.account.jpa;

import br.com.mechanic.account.entity.topic.TopicAnnotatorLinkHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicAnnotatorLinkHistoryRepository extends JpaRepository<TopicAnnotatorLinkHistory, Long> {

    long countByLink_Id(Long linkId);
}
