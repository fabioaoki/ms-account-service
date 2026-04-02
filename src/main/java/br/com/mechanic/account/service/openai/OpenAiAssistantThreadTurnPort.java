package br.com.mechanic.account.service.openai;

import org.springframework.lang.Nullable;

/**
 * Executa uma mensagem do utilizador numa thread Assistants existente ou cria uma nova thread quando {@code existingOpenAiThreadId} é nulo.
 * A thread <strong>não</strong> é apagada ao fim (contrário de {@link OpenAiAssistantCompletionClient}).
 */
public interface OpenAiAssistantThreadTurnPort {

    OpenAiAssistantThreadTurnResult runTurn(
            String assistantId,
            @Nullable String existingOpenAiThreadId,
            String userMessageJson
    );
}
