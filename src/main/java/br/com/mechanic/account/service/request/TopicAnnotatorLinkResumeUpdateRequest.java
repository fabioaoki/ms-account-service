package br.com.mechanic.account.service.request;

import br.com.mechanic.account.constant.AnnotatorLinkJsonConstants;
import br.com.mechanic.account.constant.TopicAnnotatorLinkValidationConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corpo do PUT {@code .../annotator-link/resume}: identifica o vínculo por {@code annotator_account_id}
 * e envia o texto do resumo (nominalmente o “raw” do anotador).
 */
public record TopicAnnotatorLinkResumeUpdateRequest(
        @NotNull(message = TopicAnnotatorLinkValidationConstants.MESSAGE_ANNOTATOR_ACCOUNT_ID_REQUIRED)
        @JsonProperty(AnnotatorLinkJsonConstants.ANNOTATOR_ACCOUNT_ID)
        Long annotatorAccountId,

        @NotBlank(message = TopicAnnotatorLinkValidationConstants.MESSAGE_RESUME_REQUIRED)
        @Size(
                max = TopicAnnotatorLinkValidationConstants.MAX_RESUME_CHAR_COUNT,
                message = TopicAnnotatorLinkValidationConstants.MESSAGE_RESUME_EXCEEDS_MAX_LENGTH_FOR_BEAN_VALIDATION
        )
        @JsonProperty(AnnotatorLinkJsonConstants.RESUME)
        String resume
) {
}
