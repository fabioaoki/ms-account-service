package br.com.mechanic.account.config.security;

import br.com.mechanic.account.constant.AuthJwtConstants;
import br.com.mechanic.account.constant.AuthJwtMetadataConstants;
import br.com.mechanic.account.constant.JwtClaimConstants;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public class AuthJwtProperties {

    private String secret;

    @NotBlank
    private String issuer = AuthJwtMetadataConstants.DEFAULT_ISSUER;

    @NotBlank
    private String audience = AuthJwtMetadataConstants.DEFAULT_AUDIENCE;

    @Min(AuthJwtConstants.ACCESS_TOKEN_EXPIRATION_MIN_SECONDS)
    @Max(AuthJwtConstants.ACCESS_TOKEN_EXPIRATION_MAX_SECONDS)
    private long accessTokenExpirationSeconds = AuthJwtConstants.DEFAULT_ACCESS_TOKEN_EXPIRATION_SECONDS;

    @Min(3600)
    private long refreshTokenExpirationSeconds = AuthJwtConstants.DEFAULT_REFRESH_TOKEN_EXPIRATION_SECONDS;

    @PostConstruct
    void validateSecretEntropy() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.security.jwt.secret is required (use a random value from the environment).");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < JwtClaimConstants.MIN_SECRET_BYTE_LENGTH_HMAC_SHA256) {
            throw new IllegalStateException(
                    "app.security.jwt.secret must be at least "
                            + JwtClaimConstants.MIN_SECRET_BYTE_LENGTH_HMAC_SHA256
                            + " bytes (UTF-8) for HS256. Prefer a random value from the environment (e.g.openssl rand -base64 32)."
            );
        }
    }
}
