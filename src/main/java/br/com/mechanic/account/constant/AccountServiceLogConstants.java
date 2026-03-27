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

    public static final String UPDATE_ACCOUNT_REJECTED_INACTIVE_STATUS =
            "Atualizacao da conta nao permitida: status inativo.";

    public static final String LINK_PROFILE_TO_ACCOUNT_FLOW_STARTED =
            "Fluxo de vinculo de perfil a conta iniciado. identificadorConta={} tipoPerfil={}";

    public static final String LINK_PROFILE_TO_ACCOUNT_FLOW_COMPLETED =
            "Fluxo de vinculo de perfil a conta concluido. identificadorConta={} identificadorPerfil={}";

    public static final String LINK_PROFILE_TO_ACCOUNT_REJECTED_ANNOTATOR =
            "Vinculo de perfil reprovado: ANNOTATOR nao permitido neste fluxo.";

    public static final String LINK_PROFILE_TO_ACCOUNT_REJECTED_ALREADY_LINKED =
            "Vinculo de perfil reprovado: perfil ja vinculado a conta.";

    public static final String LINK_PROFILE_TO_ACCOUNT_REJECTED_ACCOUNT_NOT_ACTIVE =
            "Vinculo de perfil reprovado: conta nao esta ACTIVE.";

    public static final String UNLINK_PROFILE_FROM_ACCOUNT_FLOW_STARTED =
            "Fluxo de desvinculo de perfil da conta iniciado. identificadorConta={} tipoPerfil={}";

    public static final String UNLINK_PROFILE_FROM_ACCOUNT_FLOW_COMPLETED =
            "Fluxo de desvinculo de perfil da conta concluido. identificadorConta={} identificadorPerfil={}";

    public static final String UNLINK_PROFILE_FROM_ACCOUNT_REJECTED_ANNOTATOR =
            "Desvinculo de perfil reprovado: ANNOTATOR nao pode ser removido.";

    public static final String UNLINK_PROFILE_FROM_ACCOUNT_IDEMPOTENT_NO_BINDING =
            "Desvinculo de perfil idempotente: vinculo ja estava ausente.";

    public static final String UNLINK_PROFILE_FROM_ACCOUNT_REJECTED_ACCOUNT_NOT_ACTIVE =
            "Desvinculo de perfil reprovado: conta nao esta ACTIVE.";

    public static final String GET_ACCOUNT_BY_ID_FLOW_STARTED =
            "Fluxo de consulta de conta por identificador iniciado. identificador={}";

    public static final String GET_ACCOUNT_BY_ID_FLOW_COMPLETED =
            "Fluxo de consulta de conta por identificador concluido. identificador={}";

    public static final String DEACTIVATE_ACCOUNT_FLOW_STARTED =
            "Fluxo de desativacao de conta iniciado. identificador={}";

    public static final String DEACTIVATE_ACCOUNT_FLOW_COMPLETED =
            "Fluxo de desativacao de conta concluido. identificador={}";

    public static final String DEACTIVATE_ACCOUNT_IDEMPOTENT_ALREADY_INACTIVE =
            "Desativacao idempotente: conta ja estava INACTIVE. identificador={}";

    public static final String ACTIVATE_ACCOUNT_FLOW_STARTED =
            "Fluxo de ativacao de conta iniciado. identificador={}";

    public static final String ACTIVATE_ACCOUNT_FLOW_COMPLETED =
            "Fluxo de ativacao de conta concluido. identificador={}";

    public static final String ACTIVATE_ACCOUNT_IDEMPOTENT_ALREADY_ACTIVE =
            "Ativacao idempotente: conta ja estava ACTIVE. identificador={}";
}
