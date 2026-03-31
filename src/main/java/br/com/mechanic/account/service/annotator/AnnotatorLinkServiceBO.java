package br.com.mechanic.account.service.annotator;

import br.com.mechanic.account.enuns.TopicStatusEnum;
import br.com.mechanic.account.service.request.AnnotatorBlockCreateRequest;
import br.com.mechanic.account.service.request.TopicAnnotatorLinkCreateRequest;
import br.com.mechanic.account.service.request.TopicAnnotatorLinkResumeUpdateRequest;
import br.com.mechanic.account.service.response.AnnotatorBlockedAccountPageResponse;
import br.com.mechanic.account.service.response.AnnotatorBlockResponse;
import br.com.mechanic.account.service.response.TopicAnnotatorLinkAnnotatorListItemResponse;
import br.com.mechanic.account.service.response.TopicAnnotatorLinkResponse;

import java.util.List;

public interface AnnotatorLinkServiceBO {

    TopicAnnotatorLinkResponse createLink(Long topicOwnerAccountId, Long topicId, TopicAnnotatorLinkCreateRequest request);

    AnnotatorBlockResponse blockAnnotatorAccount(
            Long topicOwnerAccountId,
            Long topicId,
            AnnotatorBlockCreateRequest request
    );

    AnnotatorBlockedAccountPageResponse listBlockedAnnotatorAccounts(
            Long topicOwnerAccountId,
            Integer page,
            Integer size
    );

    TopicAnnotatorLinkResponse updateLinkResume(
            Long topicOwnerAccountId,
            Long topicId,
            TopicAnnotatorLinkResumeUpdateRequest request
    );

    List<TopicAnnotatorLinkAnnotatorListItemResponse> listLinksForAnnotatorView(
            Long annotatorAccountId,
            Long topicId,
            Long topicOwnerAccountId,
            TopicStatusEnum topicStatus
    );
}
