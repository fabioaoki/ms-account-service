package br.com.mechanic.account.entity.topic;

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
@Table(name = EntityConstants.TOPIC_AI_PROCESSING_ERROR_TABLE_NAME)
public class TopicAiProcessingError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = EntityConstants.COLUMN_TOPIC_ID,
            nullable = false,
            foreignKey = @ForeignKey(name = EntityConstants.TOPIC_AI_PROCESSING_ERROR_TOPIC_FK_NAME)
    )
    private Topic topic;

    @Column(
            name = EntityConstants.COLUMN_PROBLEMATIC_TEXT,
            nullable = false,
            length = EntityConstants.TOPIC_AI_PROCESSING_ERROR_TEXT_COLUMN_LENGTH
    )
    private String problematicText;

    @Column(name = EntityConstants.COLUMN_CREATED_AT, nullable = false)
    private LocalDateTime createdAt;
}
