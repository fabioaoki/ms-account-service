package br.com.mechanic.account.service.response;

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
public class AccountUpdateResponse {

    private Long id;
    private String name;
    private LocalDate birthDate;
    private LocalDateTime lastUpdatedAt;
}

