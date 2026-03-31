package br.com.mechanic.account.security;

import br.com.mechanic.account.constant.SecurityAuthorityConstants;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Autenticação sintética para {@code MockMvc} (não valida assinatura JWT).
 */
public final class JwtTestAuthentication {

    private JwtTestAuthentication() {
    }

    public static RequestPostProcessor jwtWithAuthorities(long accountId, String... authorities) {
        Jwt jwt = Jwt.withTokenValue("integration-test-token")
                .header("alg", "none")
                .subject(String.valueOf(accountId))
                .issuedAt(Instant.now())
                .build();
        List<SimpleGrantedAuthority> auths = Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, auths, String.valueOf(accountId));
        return SecurityMockMvcRequestPostProcessors.authentication(token);
    }

    public static RequestPostProcessor ownerFull(long accountId) {
        return jwtWithAuthorities(
                accountId,
                SecurityAuthorityConstants.OWNER_FULL,
                SecurityAuthorityConstants.ANNOTATOR
        );
    }

    public static RequestPostProcessor ownerStandard(long accountId) {
        return jwtWithAuthorities(
                accountId,
                SecurityAuthorityConstants.OWNER_STANDARD,
                SecurityAuthorityConstants.ANNOTATOR
        );
    }

    public static RequestPostProcessor annotatorOnly(long accountId) {
        return jwtWithAuthorities(accountId, SecurityAuthorityConstants.ANNOTATOR);
    }
}
