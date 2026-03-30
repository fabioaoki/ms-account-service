package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnnotatorLinkJsonConstants {

    public static final String ID = "id";

    public static final String TOPIC_ID = "topic_id";

    public static final String TOPIC_OWNER_ACCOUNT_ID = "topic_owner_account_id";

    public static final String ANNOTATOR_ACCOUNT_ID = "annotator_account_id";

    public static final String RESUME = "resume";

    public static final String CREATED_AT = "created_at";

    public static final String LAST_UPDATED_AT = "last_updated_at";

    /**
     * Status do tópico: campo na resposta da listagem do anotador (pode ser nulo para perfil ANNOTATOR)
     * e nome do query param opcional {@code GET .../topic-annotator-links?topic_status=OPEN}.
     */
    public static final String TOPIC_STATUS = "topic_status";

    /** Nome formatado do criador do tópico (coluna {@code name} da conta dona). */
    public static final String TOPIC_OWNER_NAME = "topic_owner_name";
}
