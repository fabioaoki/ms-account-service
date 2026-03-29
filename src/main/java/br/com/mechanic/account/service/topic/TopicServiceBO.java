package br.com.mechanic.account.service.topic;

import br.com.mechanic.account.service.request.TopicCreateRequest;
import br.com.mechanic.account.service.request.TopicUpdateRequest;
import br.com.mechanic.account.service.response.TopicResponse;

public interface TopicServiceBO {

    TopicResponse create(Long accountId, TopicCreateRequest request);

    TopicResponse update(Long accountId, Long topicId, TopicUpdateRequest request);
}
