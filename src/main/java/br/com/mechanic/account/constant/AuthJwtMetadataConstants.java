package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Metadados padrão do JWT (iss, aud) quando não sobrescritos por configuração.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthJwtMetadataConstants {

    public static final String DEFAULT_ISSUER = "ms-account-service";

    public static final String DEFAULT_AUDIENCE = "ms-account-api-clients";
}
