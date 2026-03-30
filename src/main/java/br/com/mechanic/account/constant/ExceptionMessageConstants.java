package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionMessageConstants {

    public static final String GENERIC_REGISTRATION_FAILURE =
            "Nao foi possivel concluir o cadastro. Verifique os dados e tente novamente.";

    /**
     * Corpo JSON inválido (ex.: quebra de linha literal dentro de uma string — em JSON use {@code \n}).
     */
    public static final String MESSAGE_REQUEST_BODY_MALFORMED_JSON =
            "Malformed JSON request body. Line breaks inside string values must be escaped as \\n, not as literal newlines. "
                    + "Example: \"line one\\nline two\" or send the body from a file with jq.";
}
