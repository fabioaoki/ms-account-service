package br.com.mechanic.account.entity.account;

import br.com.mechanic.account.constant.EntityConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = EntityConstants.REFRESH_TOKEN_TABLE_NAME)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = EntityConstants.COLUMN_ACCOUNT_ID,
            nullable = false,
            foreignKey = @ForeignKey(name = EntityConstants.REFRESH_TOKEN_ACCOUNT_FK_NAME)
    )
    private Account account;

    @Column(
            name = EntityConstants.COLUMN_TOKEN_HASH,
            nullable = false,
            unique = true,
            length = EntityConstants.REFRESH_TOKEN_HASH_COLUMN_LENGTH
    )
    private String tokenHash;

    @Column(name = EntityConstants.COLUMN_EXPIRES_AT, nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = EntityConstants.COLUMN_REVOKED_AT)
    private LocalDateTime revokedAt;

    @Column(name = EntityConstants.COLUMN_CREATED_AT, nullable = false)
    private LocalDateTime createdAt;
}
