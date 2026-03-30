package br.com.mechanic.account.service.response;

import br.com.mechanic.account.constant.AnnotatorLinkJsonConstants;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TopicAnnotatorLinkAnnotatorListItemResponse(
        @JsonProperty(AnnotatorLinkJsonConstants.ID)
        Long id,
        @JsonProperty(AnnotatorLinkJsonConstants.TOPIC_ID)
        Long topicId,
        @JsonInclude(JsonInclude.Include.ALWAYS)
        @JsonProperty(AnnotatorLinkJsonConstants.TOPIC_STATUS)
        TopicStatusEnum topicStatus,
        @JsonProperty(AnnotatorLinkJsonConstants.TOPIC_OWNER_ACCOUNT_ID)
        Long topicOwnerAccountId,
        @JsonProperty(AnnotatorLinkJsonConstants.TOPIC_OWNER_NAME)
        String topicOwnerName,
        @JsonProperty(AnnotatorLinkJsonConstants.ANNOTATOR_ACCOUNT_ID)
        Long annotatorAccountId,
        @JsonProperty(AnnotatorLinkJsonConstants.RESUME)
        String resume,
        @JsonProperty(AnnotatorLinkJsonConstants.CREATED_AT)
        LocalDateTime createdAt,
        @JsonProperty(AnnotatorLinkJsonConstants.LAST_UPDATED_AT)
        LocalDateTime lastUpdatedAt
) {
}
