package br.com.mechanic.account.entity.topic;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
@Table(name = EntityConstants.TOPIC_ANNOTATOR_LINK_HISTORY_TABLE_NAME)
public class TopicAnnotatorLinkHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = EntityConstants.COLUMN_ANNOTATOR_LINK_ID,
            nullable = false,
            foreignKey = @ForeignKey(name = EntityConstants.TOPIC_ANNOTATOR_LINK_HISTORY_LINK_FK_NAME)
    )
    private TopicAnnotatorLink link;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = EntityConstants.COLUMN_ANNOTATOR_ACCOUNT_ID,
            nullable = false,
            foreignKey = @ForeignKey(name = EntityConstants.TOPIC_ANNOTATOR_LINK_HISTORY_ANNOTATOR_FK_NAME)
    )
    private Account annotatorAccount;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = EntityConstants.COLUMN_TOPIC_ANNOTATOR_LINK_RESUME, nullable = true)
    private String resume;

    @Column(name = EntityConstants.COLUMN_HISTORY_CREATED_AT, nullable = false)
    private LocalDateTime createdAt;
}
