package br.com.mechanic.account.service.response;

import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
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
public class AccountResponse {

    private Long id;
    private String email;
    private String name;
    private LocalDate birthDate;
    private AccountProfileTypeEnum profileType;
    private LocalDateTime createdAt;
}
