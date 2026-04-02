package br.com.mechanic.account.service.response;

import br.com.mechanic.account.constant.TextAiAssistantResponseJsonConstants;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @param threadId Identificador da thread na API OpenAI Assistants (mantém o histórico de mensagens).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TextAiAssistantResponse(
        @JsonProperty(TextAiAssistantResponseJsonConstants.THREAD_ID)
        String threadId,

        @JsonProperty(TextAiAssistantResponseJsonConstants.TITLE)
        String title,

        @JsonProperty(TextAiAssistantResponseJsonConstants.ALL_RESUME)
        String allResume,

        @JsonProperty(TextAiAssistantResponseJsonConstants.MODIFICATION_RESUME)
        String modificationResume,

        @JsonProperty(TextAiAssistantResponseJsonConstants.MODIFICATION)
        boolean modification,

        @JsonProperty(TextAiAssistantResponseJsonConstants.CHAT)
        String chat
) {
}
