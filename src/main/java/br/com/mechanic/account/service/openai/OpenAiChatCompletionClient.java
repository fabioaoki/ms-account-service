package br.com.mechanic.account.service.openai;

import br.com.mechanic.account.config.OpenAiProperties;
import br.com.mechanic.account.constant.OpenAiApiConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.exception.AccountException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class OpenAiChatCompletionClient implements OpenAiChatCompletionPort {

    private final RestClient restClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiChatCompletionClient(RestClient restClient, OpenAiProperties properties, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String completeChat(String systemPrompt, String userMessageJson) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_API_KEY_NOT_CONFIGURED);
        }
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", properties.getModel());
        ArrayNode messages = body.putArray("messages");
        ObjectNode sys = messages.addObject();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        ObjectNode usr = messages.addObject();
        usr.put("role", "user");
        usr.put("content", userMessageJson);
        ObjectNode responseFormat = body.putObject("response_format");
        responseFormat.put("type", OpenAiApiConstants.JSON_OBJECT_RESPONSE_TYPE);
        try {
            String requestJson = objectMapper.writeValueAsString(body);
            ResponseEntity<String> response = restClient.post()
                    .uri(OpenAiApiConstants.CHAT_COMPLETIONS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .retrieve()
                    .toEntity(String.class);
            String raw = response.getBody();
            if (raw == null || raw.isBlank()) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED);
            }
            JsonNode root = objectMapper.readTree(raw);
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED);
            }
            return content.trim();
        } catch (AccountException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED, ex);
        } catch (Exception ex) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_CHAT_COMPLETION_FAILED, ex);
        }
    }
}
