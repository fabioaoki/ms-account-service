package br.com.mechanic.account.entity.profile;

import br.com.mechanic.account.constant.EntityConstants;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
        name = EntityConstants.PROFILE_TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(
                name = EntityConstants.PROFILE_TYPE_UK,
                columnNames = EntityConstants.COLUMN_PROFILE_TYPE
        )
)
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = EntityConstants.COLUMN_PROFILE_TYPE, nullable = false, length = EntityConstants.PROFILE_TYPE_COLUMN_LENGTH)
    private AccountProfileTypeEnum profileType;

    @Column(name = EntityConstants.COLUMN_CREATED_AT, nullable = false)
    private LocalDateTime createdAt;
}
