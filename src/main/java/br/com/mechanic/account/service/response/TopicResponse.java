package br.com.mechanic.account.service.response;

import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TopicResponse(
        Long id,
        Long accountId,
        String tema,
        @JsonProperty("contexto")
        String contexto,
        LocalDateTime createdAt,
        @JsonProperty("lastUpdatedAt")
        LocalDateTime lastUpdatedAt,
        TopicStatusEnum status,
        @JsonProperty("profile_type")
        AccountProfileTypeEnum profileType
) {
}
