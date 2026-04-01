package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtClaimConstants {

    /**
     * Papel / autoridade no access token (Spring Security mapeia para {@link org.springframework.security.core.GrantedAuthority}).
     */
    public static final String ROLES = "roles";

    /** Identificador interno numérico da conta (paths da API continuam com {@code Long}). */
    public static final String ACCOUNT_ID = "account_id";

    /** Tamanho mínimo da chave UTF-8 para HMAC-SHA256 (256 bits). */
    public static final int MIN_SECRET_BYTE_LENGTH_HMAC_SHA256 = 32;
}
