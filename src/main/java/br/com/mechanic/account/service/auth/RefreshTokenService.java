package br.com.mechanic.account.service.auth;

import br.com.mechanic.account.config.security.AuthJwtProperties;
import br.com.mechanic.account.constant.RefreshTokenSecurityConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.account.RefreshToken;
import br.com.mechanic.account.repository.account.jpa.RefreshTokenRepositoryJpa;
import br.com.mechanic.account.security.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepositoryJpa refreshTokenRepositoryJpa;
    private final AuthJwtProperties authJwtProperties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void revokeAllActiveForAccount(Long accountId) {
        LocalDateTime now = LocalDateTime.now(clock);
        refreshTokenRepositoryJpa.revokeAllActiveForAccount(accountId, now);
    }

    /**
     * Gera refresh opaco, persiste apenas o hash SHA-256 e devolve o valor bruto uma vez.
     */
    @Transactional
    public String issueAndPersist(Account account) {
        byte[] raw = new byte[RefreshTokenSecurityConstants.RAW_TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        String hash = TokenHasher.sha256Hex(token);
        LocalDateTime now = LocalDateTime.now(clock);
        RefreshToken entity = RefreshToken.builder()
                .account(account)
                .tokenHash(hash)
                .expiresAt(now.plusSeconds(authJwtProperties.getRefreshTokenExpirationSeconds()))
                .createdAt(now)
                .build();
        refreshTokenRepositoryJpa.save(entity);
        return token;
    }

    /**
     * Valida hash, expiração e revoga a linha; devolve a conta se o refresh for aceite.
     */
    @Transactional
    public Optional<Account> consumeIfValid(String rawRefreshToken) {
        String hash = TokenHasher.sha256Hex(rawRefreshToken);
        return refreshTokenRepositoryJpa.findActiveByTokenHash(hash)
                .filter(row -> !row.getExpiresAt().isBefore(LocalDateTime.now(clock)))
                .map(row -> {
                    row.setRevokedAt(LocalDateTime.now(clock));
                    refreshTokenRepositoryJpa.save(row);
                    return row.getAccount();
                });
    }
}
