package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionMessageConstants {

    public static final String GENERIC_REGISTRATION_FAILURE =
            "Nao foi possivel concluir o cadastro. Verifique os dados e tente novamente.";
}
