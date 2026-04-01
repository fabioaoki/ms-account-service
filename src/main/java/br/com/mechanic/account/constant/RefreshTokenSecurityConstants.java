package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RefreshTokenSecurityConstants {

    /** Entropia do refresh opaco antes de Base64URL e hash SHA-256. */
    public static final int RAW_TOKEN_BYTE_LENGTH = 32;

    public static final String DIGEST_ALGORITHM_SHA_256 = "SHA-256";
}
