package br.com.mechanic.account.service.response;

import br.com.mechanic.account.constant.TopicCreateRequestJsonConstants;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TopicResponse(
        Long id,
        Long accountId,
        @JsonProperty(TopicCreateRequestJsonConstants.ACCOUNT_NAME)
        String accountName,
        String title,
        @JsonProperty(TopicCreateRequestJsonConstants.CONTEXT)
        String context,
        LocalDateTime createdAt,
        @JsonProperty(TopicCreateRequestJsonConstants.LAST_UPDATED_AT)
        LocalDateTime lastUpdatedAt,
        @JsonProperty(TopicCreateRequestJsonConstants.END_DATE)
        LocalDateTime endDate,
        TopicStatusEnum status,
        @JsonProperty(TopicCreateRequestJsonConstants.PROFILE_TYPE)
        AccountProfileTypeEnum profileType,
        @JsonProperty(TopicCreateRequestJsonConstants.TOPIC_ANNOTATOR_LINKS)
        List<TopicAnnotatorLinkSummaryResponse> topicAnnotatorLinks
) {
}
