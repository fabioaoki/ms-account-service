package br.com.mechanic.account.mapper.topic;

import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.topic.Topic;
import br.com.mechanic.account.entity.topic.TopicAnnotatorLink;
import br.com.mechanic.account.entity.topic.TopicAnnotatorLinkHistory;
import br.com.mechanic.account.service.response.TopicAnnotatorLinkAnnotatorListItemResponse;
import br.com.mechanic.account.service.response.TopicAnnotatorLinkResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnnotatorLinkMapper {

    public static TopicAnnotatorLinkResponse toResponse(TopicAnnotatorLink link) {
        return new TopicAnnotatorLinkResponse(
                link.getId(),
                link.getTopic().getId(),
                link.getTopicOwnerAccount().getId(),
                link.getAnnotatorAccount().getId(),
                link.getResume(),
                link.getCreatedAt(),
                link.getLastUpdatedAt()
        );
    }

    public static TopicAnnotatorLinkAnnotatorListItemResponse toAnnotatorListItem(TopicAnnotatorLink link) {
        Account owner = link.getTopicOwnerAccount();
        return new TopicAnnotatorLinkAnnotatorListItemResponse(
                link.getId(),
                link.getTopic().getId(),
                link.getTopic().getStatus(),
                owner.getId(),
                owner.getName(),
                link.getAnnotatorAccount().getId(),
                link.getResume(),
                link.getCreatedAt(),
                link.getLastUpdatedAt()
        );
    }

    public static TopicAnnotatorLink toEntityForPersist(
            Topic topic,
            Account topicOwnerAccount,
            Account annotatorAccount,
            String resumeOrNull,
            LocalDateTime createdAt
    ) {
        return TopicAnnotatorLink.builder()
                .topic(topic)
                .topicOwnerAccount(topicOwnerAccount)
                .annotatorAccount(annotatorAccount)
                .resume(resumeOrNull)
                .createdAt(createdAt)
                .lastUpdatedAt(null)
                .build();
    }

    public static TopicAnnotatorLinkHistory toInitialHistoryEntity(
            TopicAnnotatorLink persistedLink,
            Account annotatorAccount,
            String resumeOrNull,
            LocalDateTime createdAt
    ) {
        return TopicAnnotatorLinkHistory.builder()
                .link(persistedLink)
                .annotatorAccount(annotatorAccount)
                .resume(resumeOrNull)
                .createdAt(createdAt)
                .build();
    }

    /**
     * Atualiza apenas o campo de resumo no vínculo persistido (o adaptador de repositório define
     * {@code last_updated_at} no save).
     */
    public static void applyResumeToLink(TopicAnnotatorLink link, String normalizedResume) {
        link.setResume(normalizedResume);
    }
}
