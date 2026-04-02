package br.com.mechanic.account.service.textai;

import br.com.mechanic.account.constant.TextAiAssistantOpenAiResponseJsonConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TextAiAssistantAiModelResponse(
        @JsonProperty(TextAiAssistantOpenAiResponseJsonConstants.TITLE)
        String title,

        @JsonProperty(TextAiAssistantOpenAiResponseJsonConstants.ALL_RESUME)
        String allResume,

        @JsonProperty(TextAiAssistantOpenAiResponseJsonConstants.MODIFICATION_RESUME)
        String modificationResume,

        @JsonProperty(TextAiAssistantOpenAiResponseJsonConstants.MODIFICATION)
        Boolean modification,

        @JsonProperty(TextAiAssistantOpenAiResponseJsonConstants.CHAT)
        String chat
) {
}
