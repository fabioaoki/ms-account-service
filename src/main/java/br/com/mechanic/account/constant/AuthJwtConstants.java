package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * TTLs padrão do access token (curto) e refresh (longo), alinhados a {@code app.security.jwt.*}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthJwtConstants {

    public static final long SECONDS_PER_MINUTE = 60L;

    public static final long SECONDS_PER_DAY = 86400L;

    /** Access token: mínimo recomendado (5 min). */
    public static final long ACCESS_TOKEN_EXPIRATION_MIN_SECONDS = 300L;

    /** Access token: máximo recomendado (15 min). */
    public static final long ACCESS_TOKEN_EXPIRATION_MAX_SECONDS = 900L;

    public static final long DEFAULT_ACCESS_TOKEN_EXPIRATION_SECONDS = ACCESS_TOKEN_EXPIRATION_MAX_SECONDS;

    public static final long DEFAULT_REFRESH_TOKEN_EXPIRATION_SECONDS = 7L * SECONDS_PER_DAY;
}
