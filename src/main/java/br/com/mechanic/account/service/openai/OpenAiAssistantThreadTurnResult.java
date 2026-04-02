package br.com.mechanic.account.service.openai;

/**
 * Resultado de uma volta na API Assistants.
 *
 * @param openAiThreadId Id da thread na OpenAI ({@code thread_...}): é o identificador que mantém o histórico de
 *                       mensagens no lado da OpenAI; deve ser reenviado nas chamadas seguintes.
 * @param assistantMessageText Texto bruto da última mensagem do assistente (tipicamente JSON pedido ao modelo).
 */
public record OpenAiAssistantThreadTurnResult(String openAiThreadId, String assistantMessageText) {
}
