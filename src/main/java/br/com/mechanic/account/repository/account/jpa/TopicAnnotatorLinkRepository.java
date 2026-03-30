package br.com.mechanic.account.repository.account.jpa;

import br.com.mechanic.account.entity.topic.TopicAnnotatorLink;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TopicAnnotatorLinkRepository extends JpaRepository<TopicAnnotatorLink, Long> {

    @EntityGraph(attributePaths = "annotatorAccount")
    List<TopicAnnotatorLink> findAllByTopic_Id(Long topicId, Sort sort);

    @Query("""
            select l from TopicAnnotatorLink l
            join fetch l.annotatorAccount
            where l.topic.id in :topicIds
            order by l.topic.id asc, l.createdAt asc
            """)
    List<TopicAnnotatorLink> findAllWithAnnotatorAccountByTopicIdIn(@Param("topicIds") Collection<Long> topicIds);

    boolean existsByTopic_IdAndAnnotatorAccount_Id(Long topicId, Long annotatorAccountId);

    @EntityGraph(attributePaths = {"topic", "topicOwnerAccount", "annotatorAccount"})
    Optional<TopicAnnotatorLink> findByTopic_IdAndAnnotatorAccount_Id(Long topicId, Long annotatorAccountId);

    @EntityGraph(attributePaths = {"topic", "topicOwnerAccount", "annotatorAccount"})
    List<TopicAnnotatorLink> findAllByAnnotatorAccount_Id(Long annotatorAccountId, Sort sort);

    @EntityGraph(attributePaths = {"topic", "topicOwnerAccount", "annotatorAccount"})
    List<TopicAnnotatorLink> findAllByAnnotatorAccount_IdAndTopic_Id(
            Long annotatorAccountId,
            Long topicId,
            Sort sort
    );

    @EntityGraph(attributePaths = {"topic", "topicOwnerAccount", "annotatorAccount"})
    List<TopicAnnotatorLink> findAllByAnnotatorAccount_IdAndTopicOwnerAccount_Id(
            Long annotatorAccountId,
            Long topicOwnerAccountId,
            Sort sort
    );

    @EntityGraph(attributePaths = {"topic", "topicOwnerAccount", "annotatorAccount"})
    List<TopicAnnotatorLink> findAllByAnnotatorAccount_IdAndTopic_IdAndTopicOwnerAccount_Id(
            Long annotatorAccountId,
            Long topicId,
            Long topicOwnerAccountId,
            Sort sort
    );

    @EntityGraph(attributePaths = {"topic", "topicOwnerAccount", "annotatorAccount"})
    List<TopicAnnotatorLink> findAllByAnnotatorAccount_IdAndTopic_Status(
            Long annotatorAccountId,
            TopicStatusEnum topicStatus,
            Sort sort
    );

    @EntityGraph(attributePaths = {"topic", "topicOwnerAccount", "annotatorAccount"})
    List<TopicAnnotatorLink> findAllByAnnotatorAccount_IdAndTopic_IdAndTopic_Status(
            Long annotatorAccountId,
            Long topicId,
            TopicStatusEnum topicStatus,
            Sort sort
    );

    @EntityGraph(attributePaths = {"topic", "topicOwnerAccount", "annotatorAccount"})
    List<TopicAnnotatorLink> findAllByAnnotatorAccount_IdAndTopicOwnerAccount_IdAndTopic_Status(
            Long annotatorAccountId,
            Long topicOwnerAccountId,
            TopicStatusEnum topicStatus,
            Sort sort
    );

    @EntityGraph(attributePaths = {"topic", "topicOwnerAccount", "annotatorAccount"})
    List<TopicAnnotatorLink> findAllByAnnotatorAccount_IdAndTopic_IdAndTopicOwnerAccount_IdAndTopic_Status(
            Long annotatorAccountId,
            Long topicId,
            Long topicOwnerAccountId,
            TopicStatusEnum topicStatus,
            Sort sort
    );
}
