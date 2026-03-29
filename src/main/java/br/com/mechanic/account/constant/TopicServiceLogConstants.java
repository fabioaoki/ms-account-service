package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicServiceLogConstants {

    public static final String CREATE_TOPIC_FLOW_STARTED =
            "Topic creation flow started. accountId={}";

    public static final String CREATE_TOPIC_FLOW_COMPLETED =
            "Topic creation flow completed. accountId={} topicId={}";

    public static final String CREATE_TOPIC_REJECTED_ACCOUNT_NOT_ACTIVE =
            "Topic creation rejected: account is not ACTIVE.";

    public static final String CREATE_TOPIC_REJECTED_PROFILE_NOT_LINKED =
            "Topic creation rejected: no account_profile row for this account and profile_type.";

    public static final String UPDATE_TOPIC_FLOW_STARTED =
            "Topic update flow started. accountId={} topicId={}";

    public static final String UPDATE_TOPIC_FLOW_COMPLETED =
            "Topic update flow completed. accountId={} topicId={}";

    public static final String UPDATE_TOPIC_REJECTED_ACCOUNT_NOT_ACTIVE =
            "Topic update rejected: account is not ACTIVE.";

    public static final String UPDATE_TOPIC_REJECTED_PROFILE_NOT_LINKED =
            "Topic update rejected: no account_profile row for this account and profile_type.";
}
