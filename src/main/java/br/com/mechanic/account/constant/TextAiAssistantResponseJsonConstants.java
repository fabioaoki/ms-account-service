package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Resposta REST. {@link #THREAD_ID} é o identificador da <strong>thread OpenAI Assistants</strong> (histórico
 * de mensagens no lado da OpenAI), devolvido pela API após criar ou reutilizar a thread.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextAiAssistantResponseJsonConstants {

    public static final String THREAD_ID = "thread_id";

    public static final String TITLE = "title";

    public static final String ALL_RESUME = "all_resume";

    public static final String MODIFICATION_RESUME = "modification_resume";

    public static final String MODIFICATION = "modification";

    public static final String CHAT = "chat";
}
