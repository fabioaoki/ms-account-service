package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationConstants {

    public static final int EMAIL_MAX_LENGTH = 254;
    public static final int NAME_MIN_LENGTH = 2;
    public static final int NAME_MAX_LENGTH = 120;
    public static final int FIRST_NAME_MAX_LENGTH = 59;
    public static final int LAST_NAME_MAX_LENGTH = 59;
    public static final int PASSWORD_MIN_LENGTH = 4;
    public static final int PASSWORD_MAX_LENGTH = 128;
}
