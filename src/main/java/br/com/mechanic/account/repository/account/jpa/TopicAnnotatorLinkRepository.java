package br.com.mechanic.account.repository.account.jpa;

import br.com.mechanic.account.entity.topic.TopicAnnotatorLink;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicAnnotatorLinkRepository extends JpaRepository<TopicAnnotatorLink, Long> {

    boolean existsByTopic_IdAndAnnotatorAccount_Id(Long topicId, Long annotatorAccountId);

    @EntityGraph(attributePaths = {"topic", "topicOwnerAccount", "annotatorAccount"})
    Optional<TopicAnnotatorLink> findByTopic_IdAndAnnotatorAccount_Id(Long topicId, Long annotatorAccountId);
}
