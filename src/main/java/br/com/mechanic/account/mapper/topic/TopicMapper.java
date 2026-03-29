package br.com.mechanic.account.mapper.topic;

import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.topic.Topic;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import br.com.mechanic.account.service.response.TopicResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicMapper {

    /**
     * Builds a new entity for insert. {@code lastUpdatedAt} fica {@code null} até o primeiro update.
     * {@code status} e {@code endDate} seguem {@code profileType} (sem status no create request):
     * não-ANNOTATOR = {@link TopicStatusEnum#OPEN} e {@code clientEndDate}; ANNOTATOR = ambos {@code null}.
     */
    public static Topic toEntityForPersist(
            Account account,
            String normalizedTitle,
            String normalizedContext,
            AccountProfileTypeEnum profileType,
            LocalDateTime createdAt,
            LocalDateTime clientEndDate
    ) {
        if (profileType == AccountProfileTypeEnum.ANNOTATOR) {
            return Topic.builder()
                    .account(account)
                    .title(normalizedTitle)
                    .context(normalizedContext)
                    .createdAt(createdAt)
                    .lastUpdatedAt(null)
                    .status(null)
                    .endDate(null)
                    .profileType(profileType)
                    .build();
        }
        return Topic.builder()
                .account(account)
                .title(normalizedTitle)
                .context(normalizedContext)
                .createdAt(createdAt)
                .lastUpdatedAt(null)
                .status(TopicStatusEnum.OPEN)
                .endDate(clientEndDate)
                .profileType(profileType)
                .build();
    }

    public static TopicResponse toResponse(Topic topic) {
        return new TopicResponse(
                topic.getId(),
                topic.getAccount().getId(),
                topic.getTitle(),
                topic.getContext(),
                topic.getCreatedAt(),
                topic.getLastUpdatedAt(),
                topic.getEndDate(),
                topic.getStatus(),
                topic.getProfileType()
        );
    }
}
