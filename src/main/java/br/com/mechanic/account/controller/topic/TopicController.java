package br.com.mechanic.account.controller.topic;

import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.service.request.TopicCreateRequest;
import br.com.mechanic.account.service.response.TopicResponse;
import br.com.mechanic.account.service.topic.TopicServiceBO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.ACCOUNTS_BASE_PATH)
public class TopicController {

    private final TopicServiceBO topicServiceBO;

    @PostMapping(ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE + ApiPathConstants.TOPICS_SEGMENT)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TopicResponse> create(
            @PathVariable Long accountId,
            @Valid @RequestBody TopicCreateRequest request
    ) {
        TopicResponse body = topicServiceBO.create(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
