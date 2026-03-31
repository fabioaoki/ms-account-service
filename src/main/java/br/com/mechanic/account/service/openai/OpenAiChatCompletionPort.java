package br.com.mechanic.account.service.openai;

/**
 * Porta para obter JSON consolidado a partir do prompt de sistema (chat completions)
 * ou apenas do payload do utilizador (OpenAI Assistants — o {@code systemPrompt} é ignorado nesse modo).
 */
public interface OpenAiChatCompletionPort {

    String completeChat(String systemPrompt, String userMessageJson);
}
