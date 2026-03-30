package br.com.mechanic.account.service.topic;

import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import br.com.mechanic.account.service.request.TopicCreateRequest;
import br.com.mechanic.account.service.request.TopicUpdateRequest;
import br.com.mechanic.account.service.response.TopicPageResponse;
import br.com.mechanic.account.service.response.TopicResponse;

public interface TopicServiceBO {

    TopicResponse create(Long accountId, TopicCreateRequest request);

    TopicResponse update(Long accountId, Long topicId, TopicUpdateRequest request);

    TopicResponse getByTopicIdAndAccountId(Long accountId, Long topicId);

    TopicPageResponse getAllByAccountId(
            Long accountId,
            Integer page,
            Integer size,
            TopicStatusEnum statusFilter,
            AccountProfileTypeEnum profileTypeFilter
    );

    TopicResponse closeTopic(Long accountId, Long topicId);
}
