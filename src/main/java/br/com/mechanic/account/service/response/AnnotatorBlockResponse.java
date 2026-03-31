package br.com.mechanic.account.service.response;

import br.com.mechanic.account.constant.AnnotatorLinkJsonConstants;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record AnnotatorBlockResponse(
        Long id,
        @JsonProperty(AnnotatorLinkJsonConstants.TOPIC_OWNER_ACCOUNT_ID)
        Long topicOwnerAccountId,
        @JsonProperty(AnnotatorLinkJsonConstants.BLOCKED_ACCOUNT_ID)
        Long blockedAccountId,
        @JsonProperty(AnnotatorLinkJsonConstants.CREATED_AT)
        LocalDateTime createdAt
) {
}
