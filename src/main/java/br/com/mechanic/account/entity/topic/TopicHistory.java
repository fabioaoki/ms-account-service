package br.com.mechanic.account.entity.topic;

import br.com.mechanic.account.constant.EntityConstants;
import br.com.mechanic.account.entity.account.Account;
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
@Table(name = EntityConstants.TOPIC_HISTORY_TABLE_NAME)
public class TopicHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = EntityConstants.COLUMN_ACCOUNT_ID,
            nullable = false,
            foreignKey = @ForeignKey(name = EntityConstants.TOPIC_HISTORY_ACCOUNT_FK_NAME)
    )
    private Account account;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = EntityConstants.COLUMN_TOPIC_ID,
            nullable = false,
            foreignKey = @ForeignKey(name = EntityConstants.TOPIC_HISTORY_TOPIC_FK_NAME)
    )
    private Topic topic;

    @Column(name = EntityConstants.COLUMN_HISTORY_CREATED_AT, nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = EntityConstants.COLUMN_TOPIC_STATUS, nullable = false, length = EntityConstants.TOPIC_STATUS_COLUMN_LENGTH)
    private TopicStatusEnum status;
}
