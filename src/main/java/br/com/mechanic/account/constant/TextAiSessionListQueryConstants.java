package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Query parameters para {@code GET .../text-ai-assistant/sessions}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextAiSessionListQueryConstants {

    public static final String PAGE = "page";

    public static final String SIZE = "size";
}
