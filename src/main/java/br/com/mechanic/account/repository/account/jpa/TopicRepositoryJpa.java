package br.com.mechanic.account.repository.account.jpa;

import br.com.mechanic.account.entity.topic.Topic;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TopicRepositoryJpa extends JpaRepository<Topic, Long>, JpaSpecificationExecutor<Topic> {

    long countByAccount_Id(Long accountId);

    long countByAccount_IdAndStatus(Long accountId, TopicStatusEnum status);

    @EntityGraph(attributePaths = "account")
    Optional<Topic> findByIdAndAccount_Id(Long topicId, Long accountId);

    @EntityGraph(attributePaths = "account")
    @Override
    Page<Topic> findAll(Specification<Topic> spec, Pageable pageable);
}
