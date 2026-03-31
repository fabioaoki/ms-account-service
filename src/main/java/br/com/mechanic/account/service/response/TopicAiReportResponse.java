package br.com.mechanic.account.service.response;

import br.com.mechanic.account.constant.TopicAiJsonConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record TopicAiReportResponse(
        @JsonProperty(TopicAiJsonConstants.ID)
        Long id,
        @JsonProperty(TopicAiJsonConstants.TOPIC_ID)
        Long topicId,
        @JsonProperty(TopicAiJsonConstants.TOPIC_OWNER_ACCOUNT_ID)
        Long topicOwnerAccountId,
        @JsonProperty(TopicAiJsonConstants.MODEL)
        String model,
        @JsonProperty(TopicAiJsonConstants.REQUEST_PAYLOAD)
        JsonNode requestPayload,
        @JsonProperty(TopicAiJsonConstants.RESPONSE_PAYLOAD)
        JsonNode responsePayload,
        @JsonProperty(TopicAiJsonConstants.CREATED_AT)
        LocalDateTime createdAt,
        @JsonProperty(TopicAiJsonConstants.LAST_UPDATED_AT)
        LocalDateTime lastUpdatedAt
) {
}
