package br.com.mechanic.account.service.openai;

import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.exception.AccountException;

public final class DisabledOpenAiAssistantReviewClient implements OpenAiAssistantReviewPort {

    @Override
    public OpenAiAssistantReviewResult review(String firstAssistantRawResponseJson) {
        throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_REVIEW_ASSISTANT_NOT_CONFIGURED);
    }
}
