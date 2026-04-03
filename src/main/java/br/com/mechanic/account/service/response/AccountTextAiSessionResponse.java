package br.com.mechanic.account.service.response;

import br.com.mechanic.account.constant.AccountTextAiSessionResponseJsonConstants;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record AccountTextAiSessionResponse(
        Long id,
        @JsonProperty(AccountTextAiSessionResponseJsonConstants.ACCOUNT_ID)
        Long accountId,
        @JsonProperty(AccountTextAiSessionResponseJsonConstants.OPEN_AI_THREAD_ID)
        String openAiThreadId,
        String title,
        String resume,
        @JsonProperty(AccountTextAiSessionResponseJsonConstants.TIME_CONSIDERED)
        boolean timeConsidered,
        @JsonProperty(AccountTextAiSessionResponseJsonConstants.EXPECTED_MINUTES)
        int expectedMinutes,
        @JsonProperty(AccountTextAiSessionResponseJsonConstants.CREATED_AT)
        LocalDateTime createdAt,
        @JsonProperty(AccountTextAiSessionResponseJsonConstants.LAST_UPDATED_AT)
        LocalDateTime lastUpdatedAt
) {
}
