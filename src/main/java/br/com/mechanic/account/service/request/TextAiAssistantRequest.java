package br.com.mechanic.account.service.request;

import br.com.mechanic.account.constant.EntityConstants;
import br.com.mechanic.account.constant.TextAiAssistantRequestJsonConstants;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo REST: chaves em snake_case ({@code thread_id}, etc.). Alias {@code threadId} para o id da thread.
 * Primeira interação: omita {@code thread_id} e envie {@code title}, {@code resume}, {@code time}, {@code expected}.
 * Continuação: pode enviar só {@code thread_id}/{@code threadId} e {@code chat}; os demais campos são opcionais e
 * reutilizam os valores persistidos na sessão (exceto {@code resume_modification}, que não é persistido: envie a cada
 * turno se quiser indicar um trecho específico a alterar).
 */
public record TextAiAssistantRequest(
        @JsonProperty(TextAiAssistantRequestJsonConstants.THREAD_ID)
        @JsonAlias("threadId")
        String threadId,

        @Size(max = EntityConstants.TOPIC_TITLE_COLUMN_LENGTH)
        @JsonProperty(TextAiAssistantRequestJsonConstants.TITLE)
        String title,

        @Size(max = EntityConstants.TEXT_AI_SESSION_RESUME_TEXT_COLUMN_LENGTH)
        @JsonProperty(TextAiAssistantRequestJsonConstants.RESUME)
        String resume,

        @JsonProperty(TextAiAssistantRequestJsonConstants.RESUME_MODIFICATION)
        @JsonAlias({TextAiAssistantRequestJsonConstants.RESUME_MODIFICATION_CAMEL_CASE_ALIAS})
        String resumeModification,

        @JsonProperty(TextAiAssistantRequestJsonConstants.TIME)
        Boolean time,

        @JsonProperty(TextAiAssistantRequestJsonConstants.EXPECTED)
        Integer expected,

        @NotBlank
        @JsonProperty(TextAiAssistantRequestJsonConstants.CHAT)
        String chat
) {
}
