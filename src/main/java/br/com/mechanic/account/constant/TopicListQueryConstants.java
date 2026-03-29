package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Query parameters for listing topics ({@code GET .../accounts/{accountId}/topics}).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicListQueryConstants {

    public static final String STATUS = "status";

    public static final String PROFILE_TYPE = "profile_type";
}
