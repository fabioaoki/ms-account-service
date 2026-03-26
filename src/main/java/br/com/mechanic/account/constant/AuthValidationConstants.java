package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthValidationConstants {

    public static final String LOGIN_EMAIL_REGEXP = "(?i)^[^\\s@]+@[a-z0-9.-]+\\.com(\\.br)?$";

    public static final String MESSAGE_LOGIN_EMAIL_DOMAIN =
            "Informe um e-mail valido com dominio terminado em .com ou .com.br.";
}
