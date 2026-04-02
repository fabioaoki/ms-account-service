package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextAiAssistantRequestJsonConstants {

    public static final String THREAD_ID = "thread_id";

    public static final String TITLE = "title";

    public static final String RESUME = "resume";

    public static final String RESUME_MODIFICATION = "resume_modification";

    /** Alias JSON camelCase aceito na deserialização (equivalente a {@link #RESUME_MODIFICATION}). */
    public static final String RESUME_MODIFICATION_CAMEL_CASE_ALIAS = "resumeModification";

    public static final String TIME = "time";

    public static final String EXPECTED = "expected";

    public static final String CHAT = "chat";
}
