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

    public static final String ACCOUNT_EMAIL_UK = "uk_account_email";
    public static final String PROFILE_TYPE_UK = "uk_profile_type";

    public static final String ACCOUNT_HISTORY_ACCOUNT_FK_NAME = "fk_account_history_account";
    public static final String ACCOUNT_HISTORY_PROFILE_FK_NAME = "fk_account_history_profile";
    public static final String ACCOUNT_STATUS_HISTORY_ACCOUNT_FK_NAME = "fk_account_status_history_account";
    public static final String ACCOUNT_PROFILE_ACCOUNT_FK_NAME = "fk_account_profile_account";
    public static final String ACCOUNT_PROFILE_PROFILE_FK_NAME = "fk_account_profile_profile";
    public static final String TOPIC_ACCOUNT_FK_NAME = "fk_topic_account";

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
    public static final String COLUMN_PROFILE_ID = "profile_id";
    public static final String COLUMN_HISTORY_CREATED_AT = "created_at";
    public static final String COLUMN_ACCOUNT_HISTORY_ACTION = "action";
    public static final String COLUMN_ACCOUNT_STATUS_HISTORY_OCCURRED_AT = "occurred_at";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TOPIC_CONTEXT = "context";
    public static final String COLUMN_TOPIC_STATUS = "status";
    public static final String COLUMN_TOPIC_END_DATE = "end_date";

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
}
