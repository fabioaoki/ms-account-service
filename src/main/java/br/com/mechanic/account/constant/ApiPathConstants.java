package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiPathConstants {

    public static final String API_V1_PREFIX = "/api/v1";
    public static final String ACCOUNTS_SEGMENT = "/accounts";
    public static final String ACCOUNTS_BASE_PATH = API_V1_PREFIX + ACCOUNTS_SEGMENT;

    public static final String ACCOUNT_ID_PATH_VARIABLE = "/{accountId}";

    public static final String ACCOUNT_PROFILES_SEGMENT = "/profiles";

    public static final String TOPICS_SEGMENT = "/topics";

    public static final String ACCOUNT_DEACTIVATE_SEGMENT = "/deactivate";

    public static final String ACCOUNT_ACTIVATE_SEGMENT = "/activate";

    public static final String ACCOUNTS_ACCOUNT_ID_PROFILES_ANT_PATTERN =
            ACCOUNTS_BASE_PATH + "/*/profiles";

    public static final String ACCOUNTS_ACCOUNT_ID_TOPICS_ANT_PATTERN =
            ACCOUNTS_BASE_PATH + "/*/topics";

    /**
     * Um segmento após {@code /accounts} (ex.: {@code /api/v1/accounts/1}), sem corresponder a {@code .../profiles}.
     */
    public static final String ACCOUNTS_ACCOUNT_ID_GET_ANT_PATTERN = ACCOUNTS_BASE_PATH + "/*";

    public static final String ACCOUNTS_WITH_ID_WILDCARD_PATH = ACCOUNTS_BASE_PATH + "/**";
}
