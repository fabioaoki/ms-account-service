package br.com.mechanic.account.config.security;

import br.com.mechanic.account.constant.JwtClaimConstants;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(AuthJwtProperties.class)
public class JwtBeansConfig {

    private static final String JWT_KEY_ID = "ms-account-service-auth-jwt";

    @Bean
    public SecretKey authJwtSecretKey(AuthJwtProperties properties) {
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey authJwtSecretKey) {
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(authJwtSecretKey.getEncoded())
                .keyID(JWT_KEY_ID)
                .algorithm(JWSAlgorithm.HS256)
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey authJwtSecretKey, AuthJwtProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(authJwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(properties.getIssuer());
        OAuth2TokenValidator<Jwt> audience = audienceValidator(properties.getAudience());
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, audience));
        return decoder;
    }

    private static OAuth2TokenValidator<Jwt> audienceValidator(String expectedAudience) {
        return jwt -> {
            Object aud = jwt.getClaim(JwtClaimNames.AUD);
            boolean ok = false;
            if (aud instanceof String s) {
                ok = expectedAudience.equals(s);
            } else if (aud instanceof Collection<?> c) {
                ok = c.contains(expectedAudience);
            }
            if (!ok) {
                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "Invalid audience (aud)", null);
                return OAuth2TokenValidatorResult.failure(error);
            }
            return OAuth2TokenValidatorResult.success();
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
                jwt -> authoritiesFromClaim(jwt.getClaimAsStringList(JwtClaimConstants.ROLES))
        );
        converter.setPrincipalClaimName(JwtClaimNames.SUB);
        return converter;
    }

    private static Collection<GrantedAuthority> authoritiesFromClaim(List<String> raw) {
        if (raw == null) {
            return Collections.emptyList();
        }
        return raw.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toUnmodifiableList());
    }
}
