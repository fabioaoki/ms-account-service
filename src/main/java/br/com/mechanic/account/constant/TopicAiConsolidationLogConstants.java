package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicAiConsolidationLogConstants {

    public static final String CONSOLIDATION_FLOW_STARTED =
            "Topic AI consolidation started. accountId={} topicId={}";

    public static final String CONSOLIDATION_FLOW_COMPLETED =
            "Topic AI consolidation completed. accountId={} topicId={} reportId={}";

    /**
     * Prévia do corpo bruto do modelo (cortada em {@link #MODEL_RESPONSE_LOG_PREVIEW_MAX_CHARS}) para diagnóstico quando o JSON ou o schema falham.
     */
    public static final String MODEL_RESPONSE_INVALID_JSON =
            "Topic AI model response failed validation. accountId={} topicId={} contentLength={} contentPreview={} causeMessage={}";

    public static final int MODEL_RESPONSE_LOG_PREVIEW_MAX_CHARS = 16_384;

    public static final String MODEL_RESPONSE_PREVIEW_NULL_PLACEHOLDER = "(null)";

    public static final String MODEL_RESPONSE_PREVIEW_TRUNCATED_SUFFIX = "...[truncated]";
}
