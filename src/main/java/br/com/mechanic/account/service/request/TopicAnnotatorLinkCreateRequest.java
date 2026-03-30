package br.com.mechanic.account.service.request;

import br.com.mechanic.account.constant.AnnotatorLinkJsonConstants;
import br.com.mechanic.account.constant.TopicAnnotatorLinkValidationConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * Criação do vínculo tópico–anotador. O texto do resumo é enviado apenas via
 * {@link TopicAnnotatorLinkResumeUpdateRequest} (PUT {@code .../annotator-link/resume}).
 */
public record TopicAnnotatorLinkCreateRequest(
        @NotNull(message = TopicAnnotatorLinkValidationConstants.MESSAGE_ANNOTATOR_ACCOUNT_ID_REQUIRED)
        @JsonProperty(AnnotatorLinkJsonConstants.ANNOTATOR_ACCOUNT_ID)
        Long annotatorAccountId
) {
}
