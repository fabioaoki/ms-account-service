package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Campos do JSON enviado ao modelo no utilizador (consolidação de tópico).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicAiUserPayloadJsonConstants {

    public static final String TOPIC_TITLE = "topicTitle";

    public static final String TOPIC_CONTEXT = "topicContext";

    /**
     * Tipo de perfil do criador do tópico ({@link br.com.mechanic.account.enuns.AccountProfileTypeEnum#name()}).
     */
    public static final String PROFILE_TYPE = "profileType";

    public static final String ANNOTATOR_NOTES = "annotatorNotes";

    /**
     * Identificador da linha em {@code topic_annotator_link_history}.
     */
    public static final String HISTORY_ENTRY_ID = "historyEntryId";

    public static final String ANNOTATOR_ACCOUNT_ID = "annotatorAccountId";

    public static final String RESUME = "resume";
}
