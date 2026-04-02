package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Chaves do JSON enviado como conteúdo da mensagem ao assistente OpenAI (camelCase).
 * {@link #THREAD_ID} espelha a thread OpenAI quando já existe (continuação da conversa).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextAiAssistantOpenAiUserPayloadJsonConstants {

    public static final String THREAD_ID = "threadId";

    public static final String TITLE = "title";

    public static final String RESUME = "resume";

    public static final String RESUME_MODIFICATION = "resumeModification";

    public static final String TIME = "time";

    public static final String EXPECTED = "expected";

    public static final String CHAT = "chat";
}
