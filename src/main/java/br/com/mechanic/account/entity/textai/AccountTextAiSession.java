package br.com.mechanic.account.entity.textai;

import br.com.mechanic.account.constant.EntityConstants;
import br.com.mechanic.account.entity.account.Account;
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
@Table(name = EntityConstants.ACCOUNT_TEXT_AI_SESSION_TABLE_NAME)
public class AccountTextAiSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = EntityConstants.COLUMN_ACCOUNT_ID,
            nullable = false,
            foreignKey = @ForeignKey(name = EntityConstants.ACCOUNT_TEXT_AI_SESSION_ACCOUNT_FK_NAME)
    )
    private Account account;

    @Column(
            name = EntityConstants.COLUMN_OPENAI_THREAD_ID,
            length = EntityConstants.OPENAI_THREAD_ID_COLUMN_LENGTH
    )
    private String openAiThreadId;

    @Column(name = EntityConstants.COLUMN_TITLE, nullable = false, length = EntityConstants.TOPIC_TITLE_COLUMN_LENGTH)
    private String title;

    @Column(
            name = EntityConstants.COLUMN_TEXT_AI_SESSION_RESUME,
            nullable = false,
            length = EntityConstants.TEXT_AI_SESSION_RESUME_TEXT_COLUMN_LENGTH
    )
    private String resume;

    @Column(name = EntityConstants.COLUMN_TIME_CONSIDERED, nullable = false)
    private boolean timeConsidered;

    @Column(name = EntityConstants.COLUMN_EXPECTED_MINUTES, nullable = false)
    private int expectedMinutes;

    @Column(name = EntityConstants.COLUMN_CREATED_AT, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = EntityConstants.COLUMN_LAST_UPDATED_AT)
    private LocalDateTime lastUpdatedAt;
}
