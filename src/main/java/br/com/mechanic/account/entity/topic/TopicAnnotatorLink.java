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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
        name = EntityConstants.TOPIC_ANNOTATOR_LINK_TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(
                name = EntityConstants.TOPIC_ANNOTATOR_LINK_TOPIC_ANNOTATOR_UK_NAME,
                columnNames = {
                        EntityConstants.COLUMN_TOPIC_ID,
                        EntityConstants.COLUMN_ANNOTATOR_ACCOUNT_ID
                }
        )
)
public class TopicAnnotatorLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = EntityConstants.COLUMN_TOPIC_ID,
            nullable = false,
            foreignKey = @ForeignKey(name = EntityConstants.TOPIC_ANNOTATOR_LINK_TOPIC_FK_NAME)
    )
    private Topic topic;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = EntityConstants.COLUMN_TOPIC_OWNER_ACCOUNT_ID,
            nullable = false,
            foreignKey = @ForeignKey(name = EntityConstants.TOPIC_ANNOTATOR_LINK_OWNER_FK_NAME)
    )
    private Account topicOwnerAccount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = EntityConstants.COLUMN_ANNOTATOR_ACCOUNT_ID,
            nullable = false,
            foreignKey = @ForeignKey(name = EntityConstants.TOPIC_ANNOTATOR_LINK_ANNOTATOR_FK_NAME)
    )
    private Account annotatorAccount;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = EntityConstants.COLUMN_TOPIC_ANNOTATOR_LINK_RESUME, nullable = true)
    private String resume;

    @Column(name = EntityConstants.COLUMN_CREATED_AT, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = EntityConstants.COLUMN_LAST_UPDATED_AT, nullable = true)
    private LocalDateTime lastUpdatedAt;
}
