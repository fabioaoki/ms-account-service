package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountProfileLinkValidationConstants {

    public static final String MESSAGE_ANNOTATOR_CANNOT_BE_LINKED_VIA_THIS_FLOW =
            "O perfil ANNOTATOR e atribuido automaticamente no cadastro e nao pode ser vinculado por este fluxo.";

    public static final String MESSAGE_PROFILE_ALREADY_LINKED_TO_ACCOUNT =
            "Este perfil ja esta vinculado a esta conta.";

    public static final String MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_PROFILE_BINDING_OPERATIONS =
            "Apenas contas com status ACTIVE podem vincular ou desvincular perfis.";

    public static final String MESSAGE_ANNOTATOR_CANNOT_BE_UNLINKED =
            "O perfil ANNOTATOR e o perfil padrao do cadastro e nao pode ser removido.";
}
