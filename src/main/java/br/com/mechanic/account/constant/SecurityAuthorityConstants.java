package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Autoridades emitidas no JWT após login; o token deve ser reemitido quando perfis ou tópicos OPEN mudam.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityAuthorityConstants {

    /** Conta com pelo menos um tópico próprio em {@code OPEN}: acesso completo ao dono, inclusive IA. */
    public static final String OWNER_FULL = "OWNER_FULL";

    /**
     * Conta com mais de um profile vinculado e sem tópico OPEN: dono pode tópicos, resumo de apresentação,
     * perfis, etc., mas não IA.
     */
    public static final String OWNER_STANDARD = "OWNER_STANDARD";

    /** Operações no papel de anotador (vínculo a tópicos de terceiros, listagem de vínculos). */
    public static final String ANNOTATOR = "ANNOTATOR";
}
