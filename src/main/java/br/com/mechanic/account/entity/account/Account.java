package br.com.mechanic.account.entity.account;

import br.com.mechanic.account.constant.EntityConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = EntityConstants.ACCOUNT_TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(name = EntityConstants.ACCOUNT_EMAIL_UK, columnNames = EntityConstants.COLUMN_EMAIL)
)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = EntityConstants.COLUMN_EMAIL, nullable = false, length = EntityConstants.EMAIL_COLUMN_LENGTH)
    private String email;

    @Column(name = EntityConstants.COLUMN_NAME, nullable = false, length = EntityConstants.NAME_COLUMN_LENGTH)
    private String name;

    @Column(name = EntityConstants.COLUMN_FIRST_NAME, nullable = false, length = EntityConstants.FIRST_NAME_COLUMN_LENGTH)
    private String firstName;

    @Column(name = EntityConstants.COLUMN_LAST_NAME, nullable = false, length = EntityConstants.LAST_NAME_COLUMN_LENGTH)
    private String lastName;

    @Column(name = EntityConstants.COLUMN_BIRTH_DATE, nullable = false)
    private LocalDate birthDate;

    @Column(name = EntityConstants.COLUMN_PASSWORD_HASH, nullable = false, length = EntityConstants.PASSWORD_HASH_COLUMN_LENGTH)
    private String passwordHash;

    @Column(name = EntityConstants.COLUMN_CREATED_AT, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = EntityConstants.COLUMN_LAST_UPDATED_AT, nullable = true)
    private LocalDateTime lastUpdatedAt;
}
