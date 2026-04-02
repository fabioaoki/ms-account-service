package br.com.mechanic.account.service.openai;

import br.com.mechanic.account.constant.TextAiAssistantValidationConstants;
import br.com.mechanic.account.exception.AccountException;
import org.springframework.lang.Nullable;

public class DisabledOpenAiAssistantThreadTurnClient implements OpenAiAssistantThreadTurnPort {

    @Override
    public OpenAiAssistantThreadTurnResult runTurn(
            String assistantId,
            @Nullable String existingOpenAiThreadId,
            String userMessageJson
    ) {
        throw new AccountException(TextAiAssistantValidationConstants.MESSAGE_ASSISTANT_TEXT_AI_NOT_CONFIGURED);
    }
}
