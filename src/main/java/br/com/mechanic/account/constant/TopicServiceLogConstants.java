package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicServiceLogConstants {

    public static final String CREATE_TOPIC_FLOW_STARTED =
            "Fluxo de criacao de topico iniciado. identificadorConta={}";

    public static final String CREATE_TOPIC_FLOW_COMPLETED =
            "Fluxo de criacao de topico concluido. identificadorConta={} identificadorTopico={}";

    public static final String CREATE_TOPIC_REJECTED_ACCOUNT_NOT_ACTIVE =
            "Criacao de topico reprovada: conta nao esta ACTIVE.";

    public static final String CREATE_TOPIC_REJECTED_PROFILE_NOT_LINKED =
            "Criacao de topico reprovada: nenhuma linha em account_profile para esta conta e profile_type.";

    public static final String CREATE_TOPIC_REJECTED_ANNOTATOR_PROFILE =
            "Criacao de topico reprovada: perfil ANNOTATOR nao permitido neste fluxo.";
}
