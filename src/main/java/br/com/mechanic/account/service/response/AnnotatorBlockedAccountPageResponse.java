package br.com.mechanic.account.service.response;

import br.com.mechanic.account.constant.AnnotatorLinkJsonConstants;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AnnotatorBlockedAccountPageResponse(
        @JsonProperty(AnnotatorLinkJsonConstants.TOPIC_OWNER_ACCOUNT_ID)
        Long topicOwnerAccountId,
        List<AnnotatorBlockedAccountListItemResponse> content,
        long totalElements,
        int totalPages,
        int size,
        int number,
        boolean first,
        boolean last
) {
}
