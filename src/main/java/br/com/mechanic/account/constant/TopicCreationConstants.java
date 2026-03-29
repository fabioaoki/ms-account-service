package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicCreationConstants {

    /**
     * Non-{@link br.com.mechanic.account.enuns.AccountProfileTypeEnum#ANNOTATOR} topics: {@code end_date}
     * in the request must be on or before {@code creationTimestamp.plusDays(this value)} (same time of day
     * on the limit day is allowed).
     */
    public static final int NON_ANNOTATOR_END_DATE_MAX_OFFSET_DAYS = 3;
}
