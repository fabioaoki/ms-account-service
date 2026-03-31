package br.com.mechanic.account.config.security;

import br.com.mechanic.account.constant.AuthJwtConstants;
import br.com.mechanic.account.constant.JwtClaimConstants;
import jakarta.annotation.PostConstruct;
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

    @NotBlank
    private String secret;

    @Min(60)
    private long expirationSeconds = AuthJwtConstants.DEFAULT_EXPIRATION_SECONDS;

    @PostConstruct
    void validateSecretEntropy() {
        if (secret.getBytes(StandardCharsets.UTF_8).length < JwtClaimConstants.MIN_SECRET_BYTE_LENGTH_HS512) {
            throw new IllegalStateException(
                    "app.security.jwt.secret must be at least "
                            + JwtClaimConstants.MIN_SECRET_BYTE_LENGTH_HS512
                            + " bytes (UTF-8) for HS512."
            );
        }
    }
}
