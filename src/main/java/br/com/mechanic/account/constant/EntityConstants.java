package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityConstants {

    public static final String ACCOUNT_TABLE_NAME = "account";
    public static final String PROFILE_TABLE_NAME = "profile";
    public static final String ACCOUNT_PROFILE_TABLE_NAME = "account_profile";
    public static final String ACCOUNT_HISTORY_TABLE_NAME = "account_history";
    public static final String ACCOUNT_STATUS_HISTORY_TABLE_NAME = "account_status_history";
    public static final String TOPIC_TABLE_NAME = "topic";
    public static final String TOPIC_HISTORY_TABLE_NAME = "topic_history";
    public static final String TOPIC_ANNOTATOR_LINK_TABLE_NAME = "topic_annotator_link";
    public static final String TOPIC_ANNOTATOR_LINK_HISTORY_TABLE_NAME = "topic_annotator_link_history";
    public static final String TOPIC_AI_REPORT_TABLE_NAME = "topic_ai_report";
    public static final String TOPIC_AI_PROCESSING_ERROR_TABLE_NAME = "topic_ai_processing_error";
    public static final String ACCOUNT_ANNOTATOR_BLOCK_TABLE_NAME = "account_annotator_block";
    public static final String ACCOUNT_PRESENTATION_SUMMARY_TABLE_NAME = "account_presentation_summary";
    public static final String REFRESH_TOKEN_TABLE_NAME = "refresh_token";
    public static final String ACCOUNT_TEXT_AI_SESSION_TABLE_NAME = "account_text_ai_session";

    public static final String ACCOUNT_EMAIL_UK = "uk_account_email";
    public static final String PROFILE_TYPE_UK = "uk_profile_type";

    public static final String ACCOUNT_HISTORY_ACCOUNT_FK_NAME = "fk_account_history_account";
    public static final String ACCOUNT_HISTORY_PROFILE_FK_NAME = "fk_account_history_profile";
    public static final String ACCOUNT_STATUS_HISTORY_ACCOUNT_FK_NAME = "fk_account_status_history_account";
    public static final String ACCOUNT_PROFILE_ACCOUNT_FK_NAME = "fk_account_profile_account";
    public static final String ACCOUNT_PROFILE_PROFILE_FK_NAME = "fk_account_profile_profile";
    public static final String TOPIC_ACCOUNT_FK_NAME = "fk_topic_account";
    public static final String TOPIC_HISTORY_TOPIC_FK_NAME = "fk_topic_history_topic";
    public static final String TOPIC_HISTORY_ACCOUNT_FK_NAME = "fk_topic_history_account";
    public static final String TOPIC_ANNOTATOR_LINK_TOPIC_FK_NAME = "fk_tal_topic";
    public static final String TOPIC_ANNOTATOR_LINK_OWNER_FK_NAME = "fk_tal_owner";
    public static final String TOPIC_ANNOTATOR_LINK_ANNOTATOR_FK_NAME = "fk_tal_annotator";
    public static final String TOPIC_ANNOTATOR_LINK_HISTORY_LINK_FK_NAME = "fk_talh_link";
    public static final String TOPIC_ANNOTATOR_LINK_HISTORY_ANNOTATOR_FK_NAME = "fk_talh_annotator";

    public static final String TOPIC_AI_REPORT_TOPIC_FK_NAME = "fk_topic_ai_report_topic";
    public static final String TOPIC_AI_REPORT_OWNER_FK_NAME = "fk_topic_ai_report_owner";
    public static final String TOPIC_AI_PROCESSING_ERROR_TOPIC_FK_NAME = "fk_topic_ai_processing_error_topic";
    public static final String ACCOUNT_ANNOTATOR_BLOCK_BLOCKER_FK_NAME = "fk_account_annotator_block_blocker";
    public static final String ACCOUNT_ANNOTATOR_BLOCK_BLOCKED_FK_NAME = "fk_account_annotator_block_blocked";
    public static final String ACCOUNT_ANNOTATOR_BLOCK_BLOCKER_BLOCKED_UK_NAME = "uk_account_annotator_block_blocker_blocked";
    public static final String ACCOUNT_PRESENTATION_SUMMARY_ACCOUNT_FK_NAME = "fk_account_presentation_summary_account";
    public static final String ACCOUNT_PRESENTATION_SUMMARY_ACCOUNT_UK_NAME = "uk_account_presentation_summary_account";

    public static final String REFRESH_TOKEN_ACCOUNT_FK_NAME = "fk_refresh_token_account";
    public static final String ACCOUNT_TEXT_AI_SESSION_ACCOUNT_FK_NAME = "fk_account_text_ai_session_account";

    public static final String TOPIC_ANNOTATOR_LINK_TOPIC_ANNOTATOR_UK_NAME = "uk_tal_topic_annotator";

    public static final String COLUMN_OPENAI_MODEL = "openai_model";
    public static final String COLUMN_REQUEST_PAYLOAD_JSON = "request_payload_json";
    public static final String COLUMN_RESPONSE_PAYLOAD_JSON = "response_payload_json";
    public static final String COLUMN_PROBLEMATIC_TEXT = "problematic_text";

    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_FIRST_NAME = "first_name";
    public static final String COLUMN_LAST_NAME = "last_name";
    public static final String COLUMN_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_LAST_UPDATED_AT = "last_updated_at";
    public static final String COLUMN_PROFILE_TYPE = "profile_type";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_BIRTH_DATE = "birth_date";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_ACCOUNT_ID = "account_id";
    public static final String COLUMN_ACCOUNT_PUBLIC_ID = "public_id";
    public static final String COLUMN_TOKEN_HASH = "token_hash";
    public static final String COLUMN_EXPIRES_AT = "expires_at";
    public static final String COLUMN_REVOKED_AT = "revoked_at";
    public static final String COLUMN_PROFILE_ID = "profile_id";
    public static final String COLUMN_HISTORY_CREATED_AT = "created_at";
    public static final String COLUMN_ACCOUNT_HISTORY_ACTION = "action";
    public static final String COLUMN_ACCOUNT_STATUS_HISTORY_OCCURRED_AT = "occurred_at";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TOPIC_CONTEXT = "context";
    public static final String COLUMN_TOPIC_STATUS = "status";
    public static final String COLUMN_TOPIC_END_DATE = "end_date";
    public static final String COLUMN_TOPIC_ID = "topic_id";
    public static final String COLUMN_TOPIC_OWNER_ACCOUNT_ID = "topic_owner_account_id";
    public static final String COLUMN_ANNOTATOR_ACCOUNT_ID = "annotator_account_id";
    public static final String COLUMN_BLOCKER_ACCOUNT_ID = "blocker_account_id";
    public static final String COLUMN_BLOCKED_ACCOUNT_ID = "blocked_account_id";
    public static final String COLUMN_TOPIC_ANNOTATOR_LINK_RESUME = "resume";
    public static final String COLUMN_SUMMARY_TEXT = "summary_text";
    public static final String COLUMN_OPENAI_THREAD_ID = "openai_thread_id";
    public static final String COLUMN_TIME_CONSIDERED = "time_considered";
    public static final String COLUMN_EXPECTED_MINUTES = "expected_minutes";
    public static final String COLUMN_TEXT_AI_SESSION_RESUME = "resume";
    public static final String COLUMN_ANNOTATOR_LINK_ID = "link_id";

    public static final int EMAIL_COLUMN_LENGTH = 254;
    public static final int NAME_COLUMN_LENGTH = 120;
    public static final int FIRST_NAME_COLUMN_LENGTH = 59;
    public static final int LAST_NAME_COLUMN_LENGTH = 59;
    public static final int PASSWORD_HASH_COLUMN_LENGTH = 72;
    public static final int PROFILE_TYPE_COLUMN_LENGTH = 32;
    public static final int ACCOUNT_STATUS_COLUMN_LENGTH = 32;
    public static final int ACCOUNT_HISTORY_ACTION_COLUMN_LENGTH = 16;
    public static final int TOPIC_TITLE_COLUMN_LENGTH = 500;
    public static final int TOPIC_CONTEXT_COLUMN_LENGTH = 4000;
    public static final int TOPIC_STATUS_COLUMN_LENGTH = 32;

    public static final int OPENAI_MODEL_COLUMN_LENGTH = 128;

    public static final int TOPIC_AI_PROCESSING_ERROR_TEXT_COLUMN_LENGTH = 4000;
    public static final int ACCOUNT_PRESENTATION_SUMMARY_TEXT_COLUMN_LENGTH = 8000;
    public static final int REFRESH_TOKEN_HASH_COLUMN_LENGTH = 64;

    public static final int OPENAI_THREAD_ID_COLUMN_LENGTH = 128;

    public static final int TEXT_AI_SESSION_RESUME_TEXT_COLUMN_LENGTH = 16000;
}
