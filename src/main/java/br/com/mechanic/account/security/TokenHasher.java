package br.com.mechanic.account.security;

import br.com.mechanic.account.constant.RefreshTokenSecurityConstants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hash determinístico do refresh token para persistência (nunca armazenar o valor bruto).
 */
public final class TokenHasher {

    private TokenHasher() {
    }

    public static String sha256Hex(String rawToken) {
        try {
            MessageDigest md = MessageDigest.getInstance(RefreshTokenSecurityConstants.DIGEST_ALGORITHM_SHA_256);
            byte[] digest = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(RefreshTokenSecurityConstants.DIGEST_ALGORITHM_SHA_256, e);
        }
    }
}
