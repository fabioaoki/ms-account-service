package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicValidationConstants {

    public static final int MIN_TOPIC_TITLE_CHAR_COUNT_AFTER_TRIM = 2;

    public static final String MESSAGE_TOPIC_TITLE_REQUIRED =
            "Topic title is required.";

    public static final String MESSAGE_TOPIC_TITLE_INVALID_LENGTH =
            "The topic title must contain at least two characters after trimming leading and trailing spaces.";

    public static final String MESSAGE_TOPIC_TITLE_EXCEEDS_MAX_LENGTH =
            "The topic title exceeds the maximum allowed length (%d characters).";

    public static final String MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS =
            "Only accounts with ACTIVE status can access topic endpoints.";

    public static final String MESSAGE_PROFILE_TYPE_NOT_LINKED_TO_ACCOUNT =
            "The given profile type has no account_profile row for this account.";

    public static final String MESSAGE_PROFILE_TYPE_REQUIRED =
            "Profile type is required.";

    public static final String MESSAGE_PROFILE_TYPE_REQUIRED_WHEN_END_DATE_PRESENT =
            "profile_type must be present in the request body when end_date is sent.";

    public static final String MESSAGE_ANNOTATOR_TOPIC_CANNOT_SEND_END_DATE =
            "Topics with ANNOTATOR profile must not include end_date in the request body.";

    public static final String MESSAGE_END_DATE_REQUIRED_FOR_NON_ANNOTATOR_TOPIC =
            "end_date is required for non-ANNOTATOR profiles.";

    public static final String MESSAGE_END_DATE_MUST_NOT_BE_BEFORE_CREATION =
            "end_date must not be before the topic creation time.";

    public static final String MESSAGE_END_DATE_MUST_NOT_EXCEED_CREATION_PLUS_MAX_DAYS =
            "end_date must be on or before creation time plus %d days (same time on that day is allowed).";

    public static final String MESSAGE_TOPIC_NOT_FOUND_OR_NOT_OWNED =
            "Topic not found or does not belong to this account.";

    public static final String MESSAGE_ANNOTATOR_TOPIC_UPDATE_FORBIDDEN_FIELDS =
            "ANNOTATOR topic updates must not include context, profile_type, or end_date.";

    public static final String MESSAGE_NON_ANNOTATOR_TOPIC_CANNOT_CHANGE_TO_ANNOTATOR =
            "A topic created with a non-ANNOTATOR profile cannot be updated to ANNOTATOR.";

    public static final String MESSAGE_TOPIC_UPDATE_AT_LEAST_ONE_FIELD =
            "At least one of title, context, profile_type, end_date must be present in the update body.";

    public static final String MESSAGE_ANNOTATOR_TOPIC_UPDATE_TITLE_REQUIRED =
            "title is required when updating an ANNOTATOR topic.";

    public static final String MESSAGE_TOPIC_PAGE_SIZE_INVALID =
            "page size must be between %d and %d.";

    public static final String MESSAGE_TOPIC_PAGE_NUMBER_NEGATIVE =
            "page number must be zero or greater.";

    public static final String MESSAGE_ANNOTATOR_TOPIC_LIST_CANNOT_COMBINE_WITH_STATUS_FILTER =
            "Invalid list filters: ANNOTATOR topics have no persisted status; do not send status together with profile_type ANNOTATOR.";

    public static final String MESSAGE_TOPIC_CLOSE_ONLY_ALLOWED_FROM_OPEN =
            "Topic can be closed only when status is OPEN.";

    public static final String MESSAGE_TOPIC_MUST_BE_OPEN_FOR_AI_CONSOLIDATION =
            "AI consolidation requires the topic to be OPEN for profiles with workflow status.";

    public static final String MESSAGE_TOPIC_HAS_NO_ANNOTATOR_RESUMES_FOR_AI =
            "No rows in topic_annotator_link_history with non-empty resume for this topic; nothing to consolidate.";

    public static final String MESSAGE_OPENAI_NOT_ENABLED =
            "OpenAI consolidation is disabled in this environment.";

    public static final String MESSAGE_OPENAI_API_KEY_NOT_CONFIGURED =
            "OpenAI integration is enabled but the API key is not configured.";

    public static final String MESSAGE_OPENAI_REVIEW_ASSISTANT_NOT_CONFIGURED =
            "OpenAI review assistant is not configured (assistant-linguage-id).";

    public static final String MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON =
            "The model returned content that is not valid JSON.";

    public static final String MESSAGE_OPENAI_CHAT_COMPLETION_FAILED =
            "The AI provider request failed; try again later.";

    public static final String MESSAGE_OPENAI_ASSISTANT_RUN_FAILED =
            "The OpenAI assistant run did not finish successfully; review assistant configuration or try again.";

    public static final String MESSAGE_OPENAI_ASSISTANT_POLL_TIMEOUT =
            "The OpenAI assistant run timed out while waiting for completion.";

    public static final String MESSAGE_OPENAI_REVIEW_RESPONSE_NOT_VALID_JSON =
            "The review assistant returned an invalid JSON payload.";

    public static final String MESSAGE_OPENAI_REVIEW_REJECTED_MAX_ATTEMPTS =
            "The review assistant rejected the generated report for the maximum allowed attempts.";
}
