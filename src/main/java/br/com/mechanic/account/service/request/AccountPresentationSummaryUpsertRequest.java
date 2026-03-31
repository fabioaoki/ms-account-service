package br.com.mechanic.account.service.request;

import br.com.mechanic.account.constant.AccountPresentationSummaryJsonConstants;
import br.com.mechanic.account.constant.AccountPresentationSummaryValidationConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AccountPresentationSummaryUpsertRequest(
        @NotBlank(message = AccountPresentationSummaryValidationConstants.MESSAGE_SUMMARY_REQUIRED)
        @JsonProperty(AccountPresentationSummaryJsonConstants.SUMMARY)
        String summary
) {
}
