package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Padrões do access token JWT (alinhado a {@code app.security.jwt.expiration-seconds}).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthJwtConstants {

    public static final long SECONDS_PER_MINUTE = 60L;

    /** Tempo de vida padrão do access token (TTL). */
    public static final long ACCESS_TOKEN_TTL_MINUTES = 20L;

    public static final long DEFAULT_EXPIRATION_SECONDS =
            ACCESS_TOKEN_TTL_MINUTES * SECONDS_PER_MINUTE;
}
