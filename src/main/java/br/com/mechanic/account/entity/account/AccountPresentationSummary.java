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
import jakarta.persistence.UniqueConstraint;
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
        name = EntityConstants.ACCOUNT_PRESENTATION_SUMMARY_TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(
                name = EntityConstants.ACCOUNT_PRESENTATION_SUMMARY_ACCOUNT_UK_NAME,
                columnNames = EntityConstants.COLUMN_ACCOUNT_ID
        )
)
public class AccountPresentationSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = EntityConstants.COLUMN_ACCOUNT_ID,
            nullable = false,
            foreignKey = @ForeignKey(name = EntityConstants.ACCOUNT_PRESENTATION_SUMMARY_ACCOUNT_FK_NAME)
    )
    private Account account;

    @Column(
            name = EntityConstants.COLUMN_SUMMARY_TEXT,
            nullable = false,
            length = EntityConstants.ACCOUNT_PRESENTATION_SUMMARY_TEXT_COLUMN_LENGTH
    )
    private String summary;

    @Column(name = EntityConstants.COLUMN_CREATED_AT, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = EntityConstants.COLUMN_LAST_UPDATED_AT)
    private LocalDateTime lastUpdatedAt;
}
