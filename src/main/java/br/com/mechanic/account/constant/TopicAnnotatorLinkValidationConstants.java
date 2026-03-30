package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicAnnotatorLinkValidationConstants {

    public static final int MAX_RESUME_CHAR_COUNT = 100_000;

    public static final String MESSAGE_RESUME_REQUIRED =
            "resume is required.";

    /**
     * Mensagem para {@link jakarta.validation.constraints.Size} (interpolação {@code {max}}).
     */
    public static final String MESSAGE_RESUME_EXCEEDS_MAX_LENGTH_FOR_BEAN_VALIDATION =
            "resume exceeds the maximum allowed length ({max} characters).";

    public static final String MESSAGE_RESUME_EXCEEDS_MAX_LENGTH =
            "resume exceeds the maximum allowed length (%d characters).";

    public static final String MESSAGE_ANNOTATOR_ACCOUNT_ID_REQUIRED =
            "annotator_account_id is required.";

    public static final String MESSAGE_TOPIC_NOT_FOUND_OR_NOT_OWNED_FOR_LINK =
            "Topic not found or does not belong to this account.";

    public static final String MESSAGE_ANNOTATOR_MUST_DIFFER_FROM_TOPIC_OWNER_UNLESS_TOPIC_PROFILE_IS_ANNOTATOR =
            "annotator_account_id must differ from the topic owner, unless the topic was created with ANNOTATOR profile.";

    public static final String MESSAGE_TOPIC_ANNOTATOR_LINK_PAIR_ALREADY_EXISTS =
            "This annotator is already linked to this topic.";

    public static final String MESSAGE_ANNOTATOR_MUST_HAVE_ANNOTATOR_PROFILE =
            "The annotator account must have the ANNOTATOR profile.";

    public static final String MESSAGE_ACCOUNTS_MUST_BE_ACTIVE_FOR_ANNOTATOR_LINK =
            "Only ACTIVE accounts can create or receive an annotator topic link.";

    /**
     * Tópicos com workflow de status (não-{@link br.com.mechanic.account.enuns.AccountProfileTypeEnum#ANNOTATOR})
     * só aceitam vínculo enquanto {@link br.com.mechanic.account.enuns.TopicStatusEnum#OPEN}.
     */
    public static final String MESSAGE_TOPIC_MUST_BE_OPEN_FOR_ANNOTATOR_LINK =
            "Annotator links can only be created for topics with status OPEN.";
}
