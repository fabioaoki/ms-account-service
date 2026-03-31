package br.com.mechanic.account.repository.account.jpa;

import br.com.mechanic.account.entity.topic.TopicAiProcessingError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicAiProcessingErrorRepository extends JpaRepository<TopicAiProcessingError, Long> {
}
