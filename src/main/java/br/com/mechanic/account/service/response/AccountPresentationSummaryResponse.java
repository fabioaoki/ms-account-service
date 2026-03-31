package br.com.mechanic.account.service.response;

import br.com.mechanic.account.constant.AccountPresentationSummaryJsonConstants;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record AccountPresentationSummaryResponse(
        @JsonProperty(AccountPresentationSummaryJsonConstants.ACCOUNT_ID)
        Long accountId,
        @JsonProperty(AccountPresentationSummaryJsonConstants.SUMMARY)
        String summary,
        @JsonProperty(AccountPresentationSummaryJsonConstants.CREATED_AT)
        LocalDateTime createdAt,
        @JsonProperty(AccountPresentationSummaryJsonConstants.LAST_UPDATED_AT)
        LocalDateTime lastUpdatedAt
) {
}
