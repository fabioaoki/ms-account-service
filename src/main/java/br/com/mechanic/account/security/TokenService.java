package br.com.mechanic.account.security;

import br.com.mechanic.account.config.security.AuthJwtProperties;
import br.com.mechanic.account.constant.JwtClaimConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final AuthJwtProperties authJwtProperties;

    /**
     * Access token: {@code sub} = {@link UUID} público da conta; {@code account_id} numérico para paths atuais;
     * {@code roles} espelha permissões (sem dados sensíveis).
     */
    public String issueAccessToken(long accountId, UUID publicSubject, List<String> roles) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(
                authJwtProperties.getAccessTokenExpirationSeconds(),
                ChronoUnit.SECONDS
        );
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(authJwtProperties.getIssuer())
                .subject(publicSubject.toString())
                .audience(List.of(authJwtProperties.getAudience()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim(JwtClaimConstants.ACCOUNT_ID, accountId)
                .claim(JwtClaimConstants.ROLES, roles)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
