package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenAiApiConstants {

    /** Host raiz sem sufixo {@link #API_VERSION_PATH_SUFFIX} (para RestClient baseUrl). */
    public static final String DEFAULT_RESOLVED_BASE_URL = "https://api.openai.com";

    public static final String API_VERSION_PATH_SUFFIX = "/v1";

    public static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";

    public static final String JSON_OBJECT_RESPONSE_TYPE = "json_object";
}
