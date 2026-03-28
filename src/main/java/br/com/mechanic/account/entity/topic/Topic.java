package br.com.mechanic.account.entity.topic;

import br.com.mechanic.account.constant.EntityConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = EntityConstants.TOPIC_TABLE_NAME)
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = EntityConstants.COLUMN_ACCOUNT_ID,
            nullable = false,
            foreignKey = @ForeignKey(name = EntityConstants.TOPIC_ACCOUNT_FK_NAME)
    )
    private Account account;

    @Column(name = EntityConstants.COLUMN_TEMA, nullable = false, length = EntityConstants.TOPIC_TEMA_COLUMN_LENGTH)
    private String tema;

    @Column(name = EntityConstants.COLUMN_TOPIC_CONTEXT, nullable = true, length = EntityConstants.TOPIC_CONTEXT_COLUMN_LENGTH)
    private String context;

    @Column(name = EntityConstants.COLUMN_CREATED_AT, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = EntityConstants.COLUMN_LAST_UPDATED_AT, nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = EntityConstants.COLUMN_TOPIC_STATUS, nullable = false, length = EntityConstants.TOPIC_STATUS_COLUMN_LENGTH)
    private TopicStatusEnum status;

    @Enumerated(EnumType.STRING)
    @Column(name = EntityConstants.COLUMN_PROFILE_TYPE, nullable = false, length = EntityConstants.PROFILE_TYPE_COLUMN_LENGTH)
    private AccountProfileTypeEnum profileType;
}
