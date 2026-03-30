package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnnotatorLinkServiceLogConstants {

    public static final String CREATE_LINK_FLOW_STARTED =
            "Topic annotator link flow started. topicOwnerAccountId={} topicId={} annotatorAccountId={}";

    public static final String CREATE_LINK_FLOW_COMPLETED =
            "Topic annotator link flow completed. linkId={} topicId={}";
}
