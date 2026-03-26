package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountUpdateValidationConstants {

    public static final String MESSAGE_AT_LEAST_ONE_FIELD_MUST_BE_PROVIDED =
            "Informe ao menos um campo para atualizar: firstName, lastName ou birthDate.";

    public static final String MESSAGE_FIRST_NAME_REQUIRED =
            "firstName nao pode estar vazio.";

    public static final String MESSAGE_LAST_NAME_REQUIRED =
            "lastName nao pode estar vazio.";

    public static final String MESSAGE_NAME_INVALID_FORMAT =
            "Informe um nome valido contendo apenas letras e espacos.";

    public static final String MESSAGE_NAME_CANNOT_BE_SIGLA =
            "Nome nao pode ser sigla ou abreviacao.";

    public static final String MESSAGE_ACCOUNT_NOT_FOUND =
            "Conta nao encontrada.";
}

