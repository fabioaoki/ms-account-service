package br.com.mechanic.account.service.request;

import br.com.mechanic.account.constant.EntityConstants;
import br.com.mechanic.account.constant.TopicCreateRequestJsonConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Topic create body: English JSON keys ({@link TopicCreateRequestJsonConstants}).
 * Without {@code profile_type}: {@link AccountProfileTypeEnum#ANNOTATOR} ({@code title} required, {@code context}
 * optional; do not send {@code end_date}).
 */
public record TopicCreateRequest(
        @NotBlank(message = TopicValidationConstants.MESSAGE_TOPIC_TITLE_REQUIRED)
        @Size(max = EntityConstants.TOPIC_TITLE_COLUMN_LENGTH)
        @JsonProperty(TopicCreateRequestJsonConstants.TITLE)
        String title,

        @JsonProperty(TopicCreateRequestJsonConstants.CONTEXT)
        @Size(max = EntityConstants.TOPIC_CONTEXT_COLUMN_LENGTH)
        String context,

        @JsonProperty(TopicCreateRequestJsonConstants.PROFILE_TYPE)
        AccountProfileTypeEnum profileType,

        @JsonProperty(TopicCreateRequestJsonConstants.END_DATE)
        LocalDateTime endDate
) {
}
