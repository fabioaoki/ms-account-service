package br.com.mechanic.account.mapper.topic;

import br.com.mechanic.account.entity.topic.TopicAiReport;
import br.com.mechanic.account.exception.AccountException;
import br.com.mechanic.account.service.response.TopicAiReportResponse;
import br.com.mechanic.account.constant.TopicValidationConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicAiReportMapper {

    public static TopicAiReportResponse toResponse(TopicAiReport entity, ObjectMapper objectMapper) {
        try {
            JsonNode requestNode = objectMapper.readTree(entity.getRequestPayloadJson());
            JsonNode responseNode = objectMapper.readTree(entity.getResponsePayloadJson());
            return new TopicAiReportResponse(
                    entity.getId(),
                    entity.getTopic().getId(),
                    entity.getTopicOwnerAccount().getId(),
                    entity.getOpenaiModel(),
                    requestNode,
                    responseNode,
                    entity.getCreatedAt(),
                    entity.getLastUpdatedAt()
            );
        } catch (JsonProcessingException ex) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON, ex);
        }
    }
}
