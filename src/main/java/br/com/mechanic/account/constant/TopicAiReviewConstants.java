package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicAiReviewConstants {

    public static final int MAX_REVIEW_ATTEMPTS = 3;

    public static final String JSON_ACCEPT = "accept";

    public static final String JSON_PROBLEMATIC_TEXT = "problematic_text";

    public static final String DEFAULT_PROBLEMATIC_TEXT_FALLBACK =
            "Review assistant rejected the response without a problematic_text detail.";
}
