package br.com.mechanic.account.service.response;

import br.com.mechanic.account.constant.AnnotatorLinkJsonConstants;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AnnotatorBlockedAccountListItemResponse(
        @JsonProperty(AnnotatorLinkJsonConstants.BLOCKED_ACCOUNT_ID)
        Long blockedAccountId,
        @JsonProperty(AnnotatorLinkJsonConstants.BLOCKED_ACCOUNT_NAME)
        String blockedAccountName
) {
}
