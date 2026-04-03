package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Resposta REST do {@link br.com.mechanic.account.controller.textai.TextAiAssistantController} (camelCase).
 * {@link #THREAD_ID} é o identificador da <strong>thread OpenAI Assistants</strong> (histórico no lado OpenAI).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextAiAssistantResponseJsonConstants {

    public static final String THREAD_ID = "threadId";

    public static final String TITLE = "title";

    public static final String ALL_RESUME = "allResume";

    public static final String MODIFICATION_RESUME = "modificationResume";

    public static final String MODIFICATION = "modification";

    public static final String CHAT = "chat";
}
