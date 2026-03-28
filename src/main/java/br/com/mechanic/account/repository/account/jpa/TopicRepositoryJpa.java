package br.com.mechanic.account.repository.account.jpa;

import br.com.mechanic.account.entity.topic.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepositoryJpa extends JpaRepository<Topic, Long> {

    long countByAccount_Id(Long accountId);
}
