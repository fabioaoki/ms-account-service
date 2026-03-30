package br.com.mechanic.account.service.response;

import br.com.mechanic.account.constant.AnnotatorLinkJsonConstants;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TopicAnnotatorLinkSummaryResponse(
        @JsonProperty(AnnotatorLinkJsonConstants.ANNOTATOR_ACCOUNT_ID)
        Long annotatorAccountId,
        @JsonProperty(AnnotatorLinkJsonConstants.ANNOTATOR_FULL_NAME)
        String annotatorFullName,
        @JsonInclude(JsonInclude.Include.ALWAYS)
        @JsonProperty(AnnotatorLinkJsonConstants.RESUME)
        String resume,
        @JsonProperty(AnnotatorLinkJsonConstants.ANNOTATOR_ACCOUNT_STATUS)
        AccountStatusEnum annotatorAccountStatus
) {
}
