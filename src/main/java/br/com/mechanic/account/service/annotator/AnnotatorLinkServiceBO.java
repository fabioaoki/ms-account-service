package br.com.mechanic.account.service.annotator;

import br.com.mechanic.account.service.request.TopicAnnotatorLinkCreateRequest;
import br.com.mechanic.account.service.request.TopicAnnotatorLinkResumeUpdateRequest;
import br.com.mechanic.account.service.response.TopicAnnotatorLinkResponse;

public interface AnnotatorLinkServiceBO {

    TopicAnnotatorLinkResponse createLink(Long topicOwnerAccountId, Long topicId, TopicAnnotatorLinkCreateRequest request);

    TopicAnnotatorLinkResponse updateLinkResume(
            Long topicOwnerAccountId,
            Long topicId,
            TopicAnnotatorLinkResumeUpdateRequest request
    );
}
