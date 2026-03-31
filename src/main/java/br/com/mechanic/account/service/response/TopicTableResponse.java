package br.com.mechanic.account.service.response;

import br.com.mechanic.account.constant.TopicCreateRequestJsonConstants;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Campos persistidos na tabela {@code topic} (sem joins nem agregados de anotadores).
 * Usado na resposta do PUT de atualização de tópico.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TopicTableResponse(
        Long id,
        Long accountId,
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
        AccountProfileTypeEnum profileType
) {
}
