package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicValidationConstants {

    public static final int MIN_TEMA_CHAR_COUNT_AFTER_TRIM = 2;

    public static final String MESSAGE_TEMA_REQUIRED =
            "O tema e obrigatorio.";

    public static final String MESSAGE_TEMA_INVALID_LENGTH =
            "O tema deve ter pelo menos dois caracteres apos remover espacos nas extremidades.";

    public static final String MESSAGE_ACCOUNT_MUST_BE_ACTIVE_TO_CREATE_TOPIC =
            "Apenas contas com status ACTIVE podem criar topicos.";

    public static final String MESSAGE_PROFILE_TYPE_NOT_LINKED_TO_ACCOUNT =
            "O tipo de perfil informado nao possui registro na tabela account_profile para esta conta.";

    public static final String MESSAGE_ANNOTATOR_CANNOT_CREATE_TOPIC =
            "Topicos nao podem ser criados com o perfil ANNOTATOR; utilize um perfil complementar vinculado a conta.";

    public static final String MESSAGE_PROFILE_TYPE_REQUIRED =
            "O tipo de perfil e obrigatorio.";
}
