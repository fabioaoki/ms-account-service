package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthValidationConstants {

    /**
     * Permite apenas domínios que terminam em {@code .com} ou {@code .com.br} (inclui subdomínios).
     */
    public static final String LOGIN_EMAIL_REGEXP = "^[^@]+@[^@]+\\.(com(\\.br)?)$";
    public static final String MESSAGE_LOGIN_EMAIL_DOMAIN = "email deve conter um domínio válido (ex.: usuario@dominio.com).";

    public static final String MESSAGE_INVALID_CREDENTIALS = "Invalid email or password.";
    public static final String MESSAGE_ACCESS_DENIED = "You do not have permission to access this resource.";
    public static final String MESSAGE_UNAUTHORIZED = "Authentication is required.";
}
