package br.com.mechanic.account.security;

import br.com.mechanic.account.config.security.AuthJwtProperties;
import br.com.mechanic.account.constant.JwtClaimConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final AuthJwtProperties authJwtProperties;

    public String issueAccessToken(long accountId, List<String> authorities) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(authJwtProperties.getExpirationSeconds(), ChronoUnit.SECONDS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(accountId))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim(JwtClaimConstants.AUTHORITIES, authorities)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
