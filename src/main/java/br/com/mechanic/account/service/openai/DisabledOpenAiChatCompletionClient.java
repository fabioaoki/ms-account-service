package br.com.mechanic.account.service.openai;

import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.exception.AccountException;

public final class DisabledOpenAiChatCompletionClient implements OpenAiChatCompletionPort {

    @Override
    public String completeChat(String systemPrompt, String userMessageJson) {
        throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_NOT_ENABLED);
    }
}
