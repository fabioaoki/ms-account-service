package br.com.mechanic.account.constant;

import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Perfis autorizados no endpoint de colaboração de texto com IA. Inclua novos valores aqui quando o produto
 * abrir o recurso a outros papéis.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextAiAssistantAccessConstants {

    public static final Set<AccountProfileTypeEnum> ALLOWED_PROFILE_TYPES = Set.of(AccountProfileTypeEnum.BISHOP);
}
