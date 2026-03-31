package br.com.mechanic.account.service.request;

import br.com.mechanic.account.constant.AnnotatorLinkJsonConstants;
import br.com.mechanic.account.constant.TopicAnnotatorLinkValidationConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record AnnotatorBlockCreateRequest(
        @NotNull(message = TopicAnnotatorLinkValidationConstants.MESSAGE_BLOCKED_ACCOUNT_ID_REQUIRED)
        @JsonProperty(AnnotatorLinkJsonConstants.BLOCKED_ACCOUNT_ID)
        Long blockedAccountId
) {
}
