package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountTextAiSessionResponseJsonConstants {

    public static final String ACCOUNT_ID = "accountId";

    public static final String OPEN_AI_THREAD_ID = "openAiThreadId";

    public static final String TIME_CONSIDERED = "timeConsidered";

    public static final String EXPECTED_MINUTES = "expectedMinutes";

    public static final String CREATED_AT = "createdAt";

    public static final String LAST_UPDATED_AT = "lastUpdatedAt";
}
