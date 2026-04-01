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
            - `POST /api/v1/auth/login` — access + refresh (armazenado no servidor como hash)
            - `POST /api/v1/auth/refresh` — novo access + rotação do refresh
            Access: **HS256**, claims `iss`, `aud`, `sub` (UUID público da conta), `account_id`, `roles`, `jti`, sem dados sensíveis.

            **Demais rotas:** `Authorization: Bearer <access_token>`.

            **Roles no access token** (estado da conta no login/refresh; obtenha novo token após mudar perfis ou tópicos):
            - `OWNER_FULL` — dono com ao menos um tópico próprio em status OPEN (acesso completo ao dono, inclusive IA nos fluxos permitidos)
            - `OWNER_STANDARD` — dono com mais de um perfil em `account_profile` e sem tópico OPEN (tópicos, resumo de apresentação, etc., sem IA)
            - `ANNOTATOR` — papel de anotador (vínculos a tópicos, listagens restritas ao próprio `accountId` quando aplicável)

            Endpoints validam se o `accountId` do path bate com o claim `account_id` do JWT (e regras de vínculo onde aplicável) e se a role permite a operação.

            Documentação interativa: `/swagger-ui.html` (redireciona para o Swagger UI). Especificação: `/v3/api-docs`.
            """;

    public static final String BEARER_JWT_SCHEME_DESCRIPTION = """
            Access JWT (HS256) de `POST /api/v1/auth/login` ou `POST /api/v1/auth/refresh` (`access_token`).
            Validação inclui assinatura, exp, iss e aud. Header: `Authorization: Bearer <access_token>`.
            Em produção use HTTPS. Cookies HttpOnly/Secure/SameSite=Strict são opcionais no cliente.
            """;
}
