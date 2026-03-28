package br.com.mechanic.account.mapper.topic;

import br.com.mechanic.account.entity.topic.Topic;
import br.com.mechanic.account.service.response.TopicResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicMapper {

    public static TopicResponse toResponse(Topic topic) {
        return new TopicResponse(
                topic.getId(),
                topic.getAccount().getId(),
                topic.getTema(),
                topic.getContext(),
                topic.getCreatedAt(),
                topic.getLastUpdatedAt(),
                topic.getStatus(),
                topic.getProfileType()
        );
    }
}
