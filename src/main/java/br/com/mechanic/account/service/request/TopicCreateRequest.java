package br.com.mechanic.account.service.request;

import br.com.mechanic.account.constant.EntityConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TopicCreateRequest(
        @NotBlank(message = TopicValidationConstants.MESSAGE_TEMA_REQUIRED)
        @Size(max = EntityConstants.TOPIC_TEMA_COLUMN_LENGTH)
        String tema,

        @JsonProperty("contexto")
        @Size(max = EntityConstants.TOPIC_CONTEXT_COLUMN_LENGTH)
        String contexto,

        @NotNull(message = TopicValidationConstants.MESSAGE_PROFILE_TYPE_REQUIRED)
        @JsonProperty("profile_type")
        AccountProfileTypeEnum profileType
) {
}
