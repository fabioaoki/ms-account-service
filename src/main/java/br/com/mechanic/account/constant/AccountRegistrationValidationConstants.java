package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountRegistrationValidationConstants {

    public static final int MIN_REGISTRATION_AGE_YEARS = 12;

    public static final int MAX_REGISTRATION_AGE_YEARS = 90;

    public static final String MESSAGE_PASSWORD_AND_CONFIRMATION_MUST_MATCH =
            "A senha e a confirmacao de senha devem ser iguais.";

    public static final String MESSAGE_MIN_AGE_NOT_MET =
            "A idade minima para cadastro e de 12 anos completos.";

    public static final String MESSAGE_MAX_AGE_EXCEEDED =
            "A idade maxima permitida para cadastro e de 90 anos.";

    public static final String MESSAGE_BIRTH_DATE_INVALID =
            "Informe uma data de nascimento valida.";
}
