package br.com.mechanic.account.controller.annotator;

import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.service.annotator.AnnotatorLinkServiceBO;
import br.com.mechanic.account.service.request.TopicAnnotatorLinkCreateRequest;
import br.com.mechanic.account.service.request.TopicAnnotatorLinkResumeUpdateRequest;
import br.com.mechanic.account.service.response.TopicAnnotatorLinkResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.ACCOUNTS_BASE_PATH)
public class AnnotatorController {

    private final AnnotatorLinkServiceBO annotatorLinkServiceBO;

    @PostMapping(
            ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE
                    + ApiPathConstants.TOPICS_SEGMENT
                    + ApiPathConstants.TOPIC_ID_PATH_VARIABLE
                    + ApiPathConstants.ANNOTATOR_LINK_SEGMENT
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TopicAnnotatorLinkResponse> createTopicAnnotatorLink(
            @PathVariable Long accountId,
            @PathVariable Long topicId,
            @Valid @RequestBody TopicAnnotatorLinkCreateRequest request
    ) {
        TopicAnnotatorLinkResponse body = annotatorLinkServiceBO.createLink(accountId, topicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping(
            ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE
                    + ApiPathConstants.TOPICS_SEGMENT
                    + ApiPathConstants.TOPIC_ID_PATH_VARIABLE
                    + ApiPathConstants.ANNOTATOR_LINK_SEGMENT
                    + ApiPathConstants.ANNOTATOR_LINK_RESUME_SEGMENT
    )
    public ResponseEntity<TopicAnnotatorLinkResponse> updateTopicAnnotatorLinkResume(
            @PathVariable Long accountId,
            @PathVariable Long topicId,
            @Valid @RequestBody TopicAnnotatorLinkResumeUpdateRequest request
    ) {
        TopicAnnotatorLinkResponse body = annotatorLinkServiceBO.updateLinkResume(accountId, topicId, request);
        return ResponseEntity.ok(body);
    }
}
