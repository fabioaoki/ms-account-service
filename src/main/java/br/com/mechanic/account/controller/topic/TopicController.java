package br.com.mechanic.account.controller.topic;

import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.TopicListQueryConstants;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import br.com.mechanic.account.service.request.TopicCreateRequest;
import br.com.mechanic.account.service.request.TopicUpdateRequest;
import br.com.mechanic.account.service.response.TopicPageResponse;
import br.com.mechanic.account.service.response.TopicResponse;
import br.com.mechanic.account.service.topic.TopicServiceBO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.ACCOUNTS_BASE_PATH)
public class TopicController {

    private final TopicServiceBO topicServiceBO;

    @GetMapping(ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE + ApiPathConstants.TOPICS_SEGMENT)
    public ResponseEntity<TopicPageResponse> getAllByAccountId(
            @PathVariable Long accountId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(name = TopicListQueryConstants.STATUS, required = false) TopicStatusEnum status,
            @RequestParam(name = TopicListQueryConstants.PROFILE_TYPE, required = false)
            AccountProfileTypeEnum profileType
    ) {
        TopicPageResponse body = topicServiceBO.getAllByAccountId(accountId, page, size, status, profileType);
        return ResponseEntity.ok(body);
    }

    @GetMapping(
            ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE
                    + ApiPathConstants.TOPICS_SEGMENT
                    + ApiPathConstants.TOPIC_ID_PATH_VARIABLE
    )
    public ResponseEntity<TopicResponse> getByTopicIdAndAccountId(
            @PathVariable Long accountId,
            @PathVariable Long topicId
    ) {
        TopicResponse body = topicServiceBO.getByTopicIdAndAccountId(accountId, topicId);
        return ResponseEntity.ok(body);
    }

    @PostMapping(ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE + ApiPathConstants.TOPICS_SEGMENT)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TopicResponse> create(
            @PathVariable Long accountId,
            @Valid @RequestBody TopicCreateRequest request
    ) {
        TopicResponse body = topicServiceBO.create(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping(
            ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE
                    + ApiPathConstants.TOPICS_SEGMENT
                    + ApiPathConstants.TOPIC_ID_PATH_VARIABLE
    )
    public ResponseEntity<TopicResponse> update(
            @PathVariable Long accountId,
            @PathVariable Long topicId,
            @RequestBody TopicUpdateRequest request
    ) {
        TopicResponse body = topicServiceBO.update(accountId, topicId, request);
        return ResponseEntity.ok(body);
    }

    @PatchMapping(ApiPathConstants.ACCOUNT_ID_TOPICS_TOPIC_ID_CLOSE_RELATIVE_PATH)
    public ResponseEntity<TopicResponse> closeTopic(
            @PathVariable Long accountId,
            @PathVariable Long topicId
    ) {
        TopicResponse body = topicServiceBO.closeTopic(accountId, topicId);
        return ResponseEntity.ok(body);
    }
}
