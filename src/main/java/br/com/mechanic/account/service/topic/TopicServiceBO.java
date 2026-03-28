package br.com.mechanic.account.service.topic;

import br.com.mechanic.account.service.request.TopicCreateRequest;
import br.com.mechanic.account.service.response.TopicResponse;

public interface TopicServiceBO {

    TopicResponse create(Long accountId, TopicCreateRequest request);
}
