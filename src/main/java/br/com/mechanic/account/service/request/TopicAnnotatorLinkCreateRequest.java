package br.com.mechanic.account.service.request;

import br.com.mechanic.account.constant.AnnotatorLinkJsonConstants;
import br.com.mechanic.account.constant.TopicAnnotatorLinkValidationConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * {@code resume} é opcional neste fluxo; regras de tamanho/normalização permanecem em
 * {@link br.com.mechanic.account.service.annotator.AnnotatorLinkService quando enviado}.
 */
public record TopicAnnotatorLinkCreateRequest(
        @NotNull(message = TopicAnnotatorLinkValidationConstants.MESSAGE_ANNOTATOR_ACCOUNT_ID_REQUIRED)
        @JsonProperty(AnnotatorLinkJsonConstants.ANNOTATOR_ACCOUNT_ID)
        Long annotatorAccountId,

        @Size(
                max = TopicAnnotatorLinkValidationConstants.MAX_RESUME_CHAR_COUNT,
                message = TopicAnnotatorLinkValidationConstants.MESSAGE_RESUME_EXCEEDS_MAX_LENGTH_FOR_BEAN_VALIDATION
        )
        @JsonProperty(AnnotatorLinkJsonConstants.RESUME)
        String resume
) {
}
