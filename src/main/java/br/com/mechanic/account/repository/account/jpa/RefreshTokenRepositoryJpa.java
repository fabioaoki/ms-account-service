package br.com.mechanic.account.repository.account.jpa;

import br.com.mechanic.account.entity.account.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepositoryJpa extends JpaRepository<RefreshToken, Long> {

    @Query("""
            SELECT r FROM RefreshToken r
            JOIN FETCH r.account
            WHERE r.tokenHash = :tokenHash AND r.revokedAt IS NULL
            """)
    Optional<RefreshToken> findActiveByTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE RefreshToken r SET r.revokedAt = :revokedAt
            WHERE r.account.id = :accountId AND r.revokedAt IS NULL
            """)
    int revokeAllActiveForAccount(
            @Param("accountId") Long accountId,
            @Param("revokedAt") LocalDateTime revokedAt
    );
}
