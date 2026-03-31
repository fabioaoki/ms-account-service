package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicAiJsonConstants {

    public static final String ID = "id";

    public static final String TOPIC_ID = "topic_id";

    public static final String TOPIC_OWNER_ACCOUNT_ID = "topic_owner_account_id";

    public static final String MODEL = "model";

    public static final String REQUEST_PAYLOAD = "request_payload";

    public static final String RESPONSE_PAYLOAD = "response_payload";

    public static final String CREATED_AT = "created_at";

    public static final String LAST_UPDATED_AT = "last_updated_at";
}
