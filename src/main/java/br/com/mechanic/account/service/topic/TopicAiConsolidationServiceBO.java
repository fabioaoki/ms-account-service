package br.com.mechanic.account.service.topic;

import br.com.mechanic.account.service.response.TopicAiReportPageResponse;
import br.com.mechanic.account.service.response.TopicAiReportResponse;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface TopicAiConsolidationServiceBO {

    void enqueueReportGeneration(Long accountId, Long topicId);

    List<TopicAiReportResponse> listReportsByTopic(Long accountId, Long topicId);

    TopicAiReportPageResponse listReportsByOwnerAccount(Long accountId, Integer page, Integer size);

    JsonNode getLatestResponsePayload(Long accountId, Long topicId);
}
