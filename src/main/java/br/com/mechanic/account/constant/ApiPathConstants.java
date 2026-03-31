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

    public static final String TOPIC_ID_PATH_VARIABLE = "/{topicId}";

    public static final String TOPIC_CLOSE_SEGMENT = "/close";

    /**
     * Relativo ao prefixo {@link #ACCOUNTS_BASE_PATH} em controllers: {@code /{accountId}/topics/{topicId}/close}.
     */
    public static final String ACCOUNT_ID_TOPICS_TOPIC_ID_CLOSE_RELATIVE_PATH =
            ACCOUNT_ID_PATH_VARIABLE + TOPICS_SEGMENT + TOPIC_ID_PATH_VARIABLE + TOPIC_CLOSE_SEGMENT;

    /**
     * Vínculo tópico–anotador (POST cria link + primeira linha de histórico).
     */
    public static final String ANNOTATOR_LINK_SEGMENT = "/annotator-link";

    /**
     * Atualização do resumo do vínculo (PUT com {@code annotator_account_id} + {@code resume}).
     */
    public static final String ANNOTATOR_LINK_RESUME_SEGMENT = "/resume";

    /**
     * Ex.: {@code /api/v1/accounts/1/topics/42} (PUT parcial por tópico).
     */
    public static final String ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_ANT_PATTERN =
            ACCOUNTS_BASE_PATH + "/*/topics/*";

    /**
     * Ex.: {@code /api/v1/accounts/1/topics/42/close} (PATCH fechar tópico OPEN → CLOSED).
     */
    public static final String ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_CLOSE_ANT_PATTERN =
            ACCOUNTS_BASE_PATH + "/*/topics/*/close";

    /**
     * Ex.: {@code /api/v1/accounts/11/topics/99/annotator-link}.
     */
    public static final String ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_ANNOTATOR_LINK_ANT_PATTERN =
            ACCOUNTS_BASE_PATH + "/*/topics/*/annotator-link";

    /**
     * Ex.: {@code /api/v1/accounts/11/topics/99/annotator-link/resume}.
     */
    public static final String ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_ANNOTATOR_LINK_RESUME_ANT_PATTERN =
            ACCOUNTS_BASE_PATH + "/*/topics/*/annotator-link/resume";

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

    /**
     * Listagem de vínculos tópico–anotador pela conta do anotador.
     * Ex.: {@code /api/v1/accounts/5/topic-annotator-links}.
     */
    public static final String TOPIC_ANNOTATOR_LINKS_SEGMENT = "/topic-annotator-links";

    /**
     * Ex.: {@code /api/v1/accounts/5/topic-annotator-links} (GET; query opcionais {@code topic_id}, {@code topic_owner_account_id}).
     */
    public static final String ACCOUNTS_ACCOUNT_ID_TOPIC_ANNOTATOR_LINKS_ANT_PATTERN =
            ACCOUNTS_BASE_PATH + "/*/topic-annotator-links";

    public static final String TOPIC_AI_CONSOLIDATION_SEGMENT = "/ai-consolidation";

    public static final String TOPIC_AI_REPORTS_SEGMENT = "/ai-reports";

    public static final String TOPIC_AI_REPORT_LATEST_RESPONSE_SEGMENT = "/ai-report-latest-response";

    public static final String ACCOUNT_TOPIC_AI_REPORTS_SEGMENT = "/topic-ai-reports";

    public static final String ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_AI_CONSOLIDATION_ANT_PATTERN =
            ACCOUNTS_BASE_PATH + "/*/topics/*/ai-consolidation";

    public static final String ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_AI_REPORTS_ANT_PATTERN =
            ACCOUNTS_BASE_PATH + "/*/topics/*/ai-reports";

    public static final String ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_AI_REPORT_LATEST_RESPONSE_ANT_PATTERN =
            ACCOUNTS_BASE_PATH + "/*/topics/*/ai-report-latest-response";

    public static final String ACCOUNTS_ACCOUNT_ID_TOPIC_AI_REPORTS_ANT_PATTERN =
            ACCOUNTS_BASE_PATH + "/*/topic-ai-reports";
}
