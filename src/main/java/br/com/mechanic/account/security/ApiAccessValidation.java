package br.com.mechanic.account.security;

import br.com.mechanic.account.constant.AuthValidationConstants;
import br.com.mechanic.account.constant.JwtClaimConstants;
import br.com.mechanic.account.constant.SecurityAuthorityConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Stream;

@Component
public class ApiAccessValidation {

    @Value("${app.security.integration-test-disable-jwt:false}")
    private boolean integrationTestDisableJwt;

    public void requireReadableAccount(Long accountId) {
        if (integrationTestDisableJwt) {
            return;
        }
        long principalId = requireJwtPrincipalAccountId();
        assertPathAccountMatches(principalId, accountId);
        if (!hasAnyAuthority(
                SecurityAuthorityConstants.ANNOTATOR,
                SecurityAuthorityConstants.OWNER_STANDARD,
                SecurityAuthorityConstants.OWNER_FULL
        )) {
            throw new AccessDeniedException(AuthValidationConstants.MESSAGE_ACCESS_DENIED);
        }
    }

    public void requireOwnerStandardOrFull(Long accountId) {
        if (integrationTestDisableJwt) {
            return;
        }
        long principalId = requireJwtPrincipalAccountId();
        assertPathAccountMatches(principalId, accountId);
        if (!hasAnyAuthority(
                SecurityAuthorityConstants.OWNER_STANDARD,
                SecurityAuthorityConstants.OWNER_FULL
        )) {
            throw new AccessDeniedException(AuthValidationConstants.MESSAGE_ACCESS_DENIED);
        }
    }

    public void requireOwnerFull(Long accountId) {
        if (integrationTestDisableJwt) {
            return;
        }
        long principalId = requireJwtPrincipalAccountId();
        assertPathAccountMatches(principalId, accountId);
        if (!hasAuthority(SecurityAuthorityConstants.OWNER_FULL)) {
            throw new AccessDeniedException(AuthValidationConstants.MESSAGE_ACCESS_DENIED);
        }
    }

    public void requireAnnotatorListingOwnAccount(Long annotatorAccountIdInPath) {
        if (integrationTestDisableJwt) {
            return;
        }
        long principalId = requireJwtPrincipalAccountId();
        assertPathAccountMatches(principalId, annotatorAccountIdInPath);
        if (!hasAuthority(SecurityAuthorityConstants.ANNOTATOR)) {
            throw new AccessDeniedException(AuthValidationConstants.MESSAGE_ACCESS_DENIED);
        }
    }

    public void requireAnnotatorLinkParticipant(Long topicOwnerAccountId, Long annotatorAccountIdFromRequest) {
        if (integrationTestDisableJwt) {
            return;
        }
        long principalId = requireJwtPrincipalAccountId();
        boolean asTopicOwner = hasAnyAuthority(
                SecurityAuthorityConstants.OWNER_STANDARD,
                SecurityAuthorityConstants.OWNER_FULL
        ) && principalId == topicOwnerAccountId;
        boolean asAnnotator = hasAuthority(SecurityAuthorityConstants.ANNOTATOR)
                && principalId == annotatorAccountIdFromRequest;
        if (!asTopicOwner && !asAnnotator) {
            throw new AccessDeniedException(AuthValidationConstants.MESSAGE_ACCESS_DENIED);
        }
    }

    private static void assertPathAccountMatches(long principalId, Long accountIdInPath) {
        if (accountIdInPath == null || accountIdInPath.longValue() != principalId) {
            throw new AccessDeniedException(AuthValidationConstants.MESSAGE_ACCESS_DENIED);
        }
    }

    private static long requireJwtPrincipalAccountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth) || !authentication.isAuthenticated()) {
            throw new AccessDeniedException(AuthValidationConstants.MESSAGE_ACCESS_DENIED);
        }
        Jwt jwt = jwtAuth.getToken();
        Object claim = jwt.getClaim(JwtClaimConstants.ACCOUNT_ID);
        if (claim instanceof Number n) {
            return n.longValue();
        }
        throw new AccessDeniedException(AuthValidationConstants.MESSAGE_ACCESS_DENIED);
    }

    private static boolean hasAuthority(String authority) {
        return streamAuthorities().anyMatch(a -> a.getAuthority().equals(authority));
    }

    private static boolean hasAnyAuthority(String... authorities) {
        return streamAuthorities().anyMatch(
                a -> Arrays.stream(authorities).anyMatch(expected -> expected.equals(a.getAuthority()))
        );
    }

    private static Stream<GrantedAuthority> streamAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Stream.empty();
        }
        return authentication.getAuthorities().stream().map(GrantedAuthority.class::cast);
    }
}
