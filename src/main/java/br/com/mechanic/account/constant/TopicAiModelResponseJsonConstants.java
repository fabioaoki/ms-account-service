package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicAiModelResponseJsonConstants {

    public static final String SUMMARY = "summary";

    /**
     * No JSON de saída: eco do contexto do tópico (nullable; multilinha).
     */
    public static final String RESPONSE_CONTEXT = "context";

    public static final String SEGMENTS = "segments";

    public static final String TEXT = "text";

    public static final String CONTRIBUTIONS = "contributions";

    public static final String EXCERPT = "excerpt";

    public static final String ANNOTATOR_ACCOUNT_IDS = "annotatorAccountIds";

    public static final String ANNOTATOR_RAW = "annotator_raw";

    public static final String GROUP_TITLE = "groupTitle";

    public static final String CONSOLIDATION_INSIGHT = "consolidationInsight";

    public static final String BIBLE_REFERENCES = "bibleReferences";

    public static final String REFERENCE_TEXT = "referenceText";

    public static final String VERSE_LIMIT = "verseLimit";

    public static final String MODERATION = "moderation";

    public static final String REMOVED_ITEMS = "removedItems";

    public static final String NOTES = "notes";
}
