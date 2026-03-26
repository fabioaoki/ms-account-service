package br.com.mechanic.account.model.account;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AccountModel {

    private final String email;
    private final String name;
    private final String firstName;
    private final String lastName;
    private final LocalDate birthDate;
    private final String passwordHash;
}
