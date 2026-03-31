package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtClaimConstants {

    public static final String AUTHORITIES = "authorities";
    public static final int MIN_SECRET_BYTE_LENGTH_HS512 = 64;
}
