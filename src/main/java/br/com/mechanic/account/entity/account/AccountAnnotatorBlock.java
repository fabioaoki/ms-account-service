package br.com.mechanic.account.entity.account;

import br.com.mechanic.account.constant.EntityConstants;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
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
@Table(
        name = EntityConstants.ACCOUNT_ANNOTATOR_BLOCK_TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(
                name = EntityConstants.ACCOUNT_ANNOTATOR_BLOCK_BLOCKER_BLOCKED_UK_NAME,
                columnNames = {
                        EntityConstants.COLUMN_BLOCKER_ACCOUNT_ID,
                        EntityConstants.COLUMN_BLOCKED_ACCOUNT_ID
                }
        )
)
public class AccountAnnotatorBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = EntityConstants.COLUMN_BLOCKER_ACCOUNT_ID,
            nullable = false,
            foreignKey = @ForeignKey(name = EntityConstants.ACCOUNT_ANNOTATOR_BLOCK_BLOCKER_FK_NAME)
    )
    private Account blockerAccount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = EntityConstants.COLUMN_BLOCKED_ACCOUNT_ID,
            nullable = false,
            foreignKey = @ForeignKey(name = EntityConstants.ACCOUNT_ANNOTATOR_BLOCK_BLOCKED_FK_NAME)
    )
    private Account blockedAccount;

    @Column(name = EntityConstants.COLUMN_CREATED_AT, nullable = false)
    private LocalDateTime createdAt;
}
