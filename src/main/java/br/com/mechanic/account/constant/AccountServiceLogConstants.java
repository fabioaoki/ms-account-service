package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountServiceLogConstants {

    public static final String CREATE_ACCOUNT_FLOW_STARTED =
            "Fluxo de cadastro de conta iniciado.";

    public static final String CREATE_ACCOUNT_REJECTED_BUSINESS_RULE =
            "Cadastro de conta nao concluido por regra de negocio.";

    public static final String CREATE_ACCOUNT_FLOW_COMPLETED =
            "Cadastro de conta concluido com sucesso. identificador={}";

    public static final String VALIDATE_BIRTH_DATE_FLOW_STARTED =
            "Inicio da validacao da data de nascimento.";

    public static final String VALIDATE_BIRTH_DATE_FLOW_COMPLETED =
            "Validacao da data de nascimento concluida com sucesso.";

    public static final String VALIDATE_BIRTH_DATE_REJECTED_FUTURE =
            "Validacao de data de nascimento reprovada: data informada esta no futuro.";

    public static final String VALIDATE_BIRTH_DATE_REJECTED_BELOW_MIN_AGE =
            "Validacao de data de nascimento reprovada: idade abaixo do minimo permitido.";

    public static final String VALIDATE_BIRTH_DATE_REJECTED_ABOVE_MAX_AGE =
            "Validacao de data de nascimento reprovada: idade acima do maximo permitido.";

    public static final String UPDATE_ACCOUNT_FLOW_STARTED =
            "Fluxo de atualizacao da conta iniciado. identificador={}";

    public static final String UPDATE_ACCOUNT_FLOW_COMPLETED =
            "Fluxo de atualizacao da conta concluido com sucesso. identificador={}";
}
