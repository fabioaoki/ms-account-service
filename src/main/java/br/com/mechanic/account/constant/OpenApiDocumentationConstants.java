package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Textos e identificadores usados na documentação OpenAPI (Swagger UI).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenApiDocumentationConstants {

    public static final String BEARER_JWT_SCHEME_NAME = "bearer-jwt";

    public static final String HTTP_SCHEME_BEARER = "bearer";

    public static final String BEARER_FORMAT_JWT = "JWT";

    public static final String API_TITLE = "ms-account-service API";

    public static final String API_VERSION = "0.0.1";

    /**
     * Descrição geral: rotas públicas, JWT e perfis do token.
     */
    public static final String API_DESCRIPTION = """
            Base REST em `/api/v1`.

            **Rotas públicas (sem `Authorization`):**
            - `POST /api/v1/accounts` — cadastro
            - `POST /api/v1/auth/login` — login (email e senha); resposta inclui `access_token` e `authorities`

            **Demais rotas:** envie `Authorization: Bearer <access_token>`.

            **Autoridades possíveis no JWT** (refletem o estado da conta no momento do login; faça login novamente após vincular perfis ou abrir tópicos):
            - `OWNER_FULL` — dono com ao menos um tópico próprio em status OPEN (acesso completo ao dono, inclusive IA nos fluxos permitidos)
            - `OWNER_STANDARD` — dono com mais de um perfil em `account_profile` e sem tópico OPEN (tópicos, resumo de apresentação, etc., sem IA)
            - `ANNOTATOR` — papel de anotador (vínculos a tópicos, listagens restritas ao próprio `accountId` quando aplicável)

            Endpoints específicos ainda validam se o `accountId` do path (ou participação em vínculo) bate com o `sub` do JWT e se a autoridade do token permite a operação (403 quando não permitido).

            Documentação interativa: `/swagger-ui.html` (redireciona para o Swagger UI). Especificação: `/v3/api-docs`.
            """;

    public static final String BEARER_JWT_SCHEME_DESCRIPTION = """
            Token JWT retornado por `POST /api/v1/auth/login` (campo `access_token`).
            Tipo: Bearer. Inclua no header `Authorization: Bearer <token>`.
            """;
}
