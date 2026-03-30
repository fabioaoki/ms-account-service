package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * JSON property names for topic API: {@link br.com.mechanic.account.service.request.TopicCreateRequest},
 * {@link br.com.mechanic.account.service.request.TopicUpdateRequest}, {@link br.com.mechanic.account.service.response.TopicResponse}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicCreateRequestJsonConstants {

    public static final String TITLE = "title";

    public static final String CONTEXT = "context";

    /** When omitted, request is treated as {@link br.com.mechanic.account.enuns.AccountProfileTypeEnum#ANNOTATOR}. */
    public static final String PROFILE_TYPE = "profile_type";

    public static final String END_DATE = "end_date";

    public static final String LAST_UPDATED_AT = "lastUpdatedAt";

    /** Nome formatado do criador do tópico ({@code account.name}), presente em {@link br.com.mechanic.account.service.response.TopicResponse}. */
    public static final String ACCOUNT_NAME = "account_name";
}
