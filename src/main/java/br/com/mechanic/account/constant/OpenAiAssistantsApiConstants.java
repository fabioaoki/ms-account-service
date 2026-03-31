package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenAiAssistantsApiConstants {

    public static final String HEADER_OPENAI_BETA = "OpenAI-Beta";

    public static final String HEADER_VALUE_ASSISTANTS_V2 = "assistants=v2";

    public static final String THREADS_RESOURCE_PATH = "/v1/threads";

    public static final long RUN_POLL_INTERVAL_MILLIS = 500L;

    public static final int RUN_POLL_MAX_ATTEMPTS = 180;

    public static final int MESSAGES_LIST_LIMIT = 16;

    public static final String QUERY_PARAM_ORDER = "order";

    public static final String QUERY_PARAM_LIMIT = "limit";

    public static final String MESSAGES_ORDER_DESC = "desc";

    public static final String JSON_FIELD_ID = "id";

    public static final String JSON_FIELD_STATUS = "status";

    public static final String JSON_FIELD_DATA = "data";

    public static final String JSON_FIELD_ROLE = "role";

    public static final String JSON_FIELD_CONTENT = "content";

    public static final String JSON_FIELD_TYPE = "type";

    public static final String JSON_FIELD_TEXT = "text";

    public static final String JSON_FIELD_VALUE = "value";

    public static final String MESSAGE_ROLE_USER = "user";

    public static final String MESSAGE_ROLE_ASSISTANT = "assistant";

    public static final String CONTENT_TYPE_TEXT = "text";

    public static final String RUN_STATUS_QUEUED = "queued";

    public static final String RUN_STATUS_IN_PROGRESS = "in_progress";

    public static final String RUN_STATUS_REQUIRES_ACTION = "requires_action";

    public static final String RUN_STATUS_COMPLETED = "completed";

    public static final String RUN_STATUS_FAILED = "failed";

    public static final String RUN_STATUS_CANCELLED = "cancelled";

    public static final String RUN_STATUS_EXPIRED = "expired";

    public static final String RUN_STATUS_INCOMPLETE = "incomplete";

    public static final String JSON_BODY_KEY_ROLE = "role";

    public static final String JSON_BODY_KEY_CONTENT = "content";

    public static final String JSON_BODY_KEY_ASSISTANT_ID = "assistant_id";

    public static String buildThreadMessagesPath(String threadId) {
        return THREADS_RESOURCE_PATH + "/" + threadId + "/messages";
    }

    public static String buildThreadRunsPath(String threadId) {
        return THREADS_RESOURCE_PATH + "/" + threadId + "/runs";
    }

    public static String buildThreadRunStatusPath(String threadId, String runId) {
        return THREADS_RESOURCE_PATH + "/" + threadId + "/runs/" + runId;
    }

    public static String buildThreadPath(String threadId) {
        return THREADS_RESOURCE_PATH + "/" + threadId;
    }
}
