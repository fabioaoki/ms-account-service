package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Textos para {@code @Tag} e {@code @Operation} nos controllers (Swagger UI).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenApiOperationDocumentationConstants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Tag {

        public static final String AUTH_NAME = "Autenticação";

        public static final String AUTH_DESCRIPTION = """
                Login e refresh: access JWT curto (HS256, iss/aud) + refresh persistido (hash no banco). \
                **Authorize** no Swagger com o `access_token` apenas.""";

        public static final String ACCOUNTS_NAME = "Contas";

        public static final String ACCOUNTS_DESCRIPTION = """
                Cadastro público (POST sem token) e gestão da conta autenticada: consulta, ativação, perfis, \
                resumo de apresentação. O `accountId` do path deve coincidir com o claim `account_id` do access token, \
                salvo regras específicas por operação.""";

        public static final String TOPICS_NAME = "Tópicos e IA";

        public static final String TOPICS_DESCRIPTION = """
                Tópicos do dono (`accountId`), consolidação IA, relatórios e bloqueio de anotadores por tópico. \
                Requer JWT; operações de IA exigem autoridade `OWNER_FULL` no serviço.""";

        public static final String ANNOTATORS_NAME = "Anotadores";

        public static final String ANNOTATORS_DESCRIPTION = """
                Vínculos tópico–anotador e atualização de resumo do vínculo. Participação validada no token \
                (dono do tópico ou conta anotadora conforme a rota).""";

        public static final String TEXT_AI_ASSISTANT_NAME = "Texto com IA (thread OpenAI)";

        public static final String TEXT_AI_ASSISTANT_DESCRIPTION = """
                Colaboração em texto (ex.: preparação de palestra) com assistente OpenAI. Respostas deste recurso em **camelCase** \
                (ex.: `threadId` devolvido pelo POST é o id da thread na API Assistants). Perfis autorizados em \
                `TextAiAssistantAccessConstants`.""";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Auth {

        public static final String LOGIN_SUMMARY = "Login (email e senha)";

        public static final String LOGIN_DESCRIPTION = """
                Retorna `access_token`, `refresh_token`, `expires_in_seconds`, `refresh_expires_in_seconds`, \
                `token_type`, `authorities` (espelho das `roles` no JWT). Novo login revoga refresh tokens ativos \
                da conta. Access: `sub`=UUID público, `account_id`, `roles`, `iss`, `aud`, `jti`. Sem senha no payload.""";

        public static final String REFRESH_SUMMARY = "Renovar access token (refresh token)";

        public static final String REFRESH_DESCRIPTION = """
                Corpo: `refresh_token`. Rotação: o refresh usado é revogado e um novo par access+refresh é emitido. \
                Resposta no mesmo formato do login.""";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Account {

        public static final String CREATE_SUMMARY = "Cadastrar conta";

        public static final String CREATE_DESCRIPTION = """
                Público: não envie `Authorization`. Após cadastrar, use POST `/api/v1/auth/login` para obter o JWT.""";

        public static final String GET_BY_ID_SUMMARY = "Consultar conta por id";

        public static final String GET_BY_ID_DESCRIPTION = """
                JWT obrigatório. Exige que o subject do token seja o próprio `accountId` e autoridade de leitura.""";

        public static final String DEACTIVATE_SUMMARY = "Desativar conta";

        public static final String ACTIVATE_SUMMARY = "Reativar conta";

        public static final String UPDATE_SUMMARY = "Atualizar dados da conta";

        public static final String LINK_PROFILE_SUMMARY = "Vincular perfil à conta";

        public static final String LINK_PROFILE_DESCRIPTION = """
                Permite ampliar papéis da conta; após vincular, faça login de novo para refletir no JWT.""";

        public static final String UNLINK_PROFILE_SUMMARY = "Desvincular perfil da conta";

        public static final String CREATE_PRESENTATION_SUMMARY = "Criar resumo de apresentação";

        public static final String UPDATE_PRESENTATION_SUMMARY = "Atualizar resumo de apresentação";

        public static final String GET_PRESENTATION_SUMMARY = "Consultar resumo de apresentação";

        public static final String OWNER_STANDARD_OR_FULL_NOTE = """
                Exige `OWNER_STANDARD` ou `OWNER_FULL` quando aplicável à regra de negócio do serviço.""";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Topic {

        public static final String LIST_SUMMARY = "Listar tópicos da conta (paginado)";

        public static final String GET_SUMMARY = "Obter tópico por id";

        public static final String CREATE_SUMMARY = "Criar tópico";

        public static final String CREATE_DESCRIPTION = Account.OWNER_STANDARD_OR_FULL_NOTE;

        public static final String UPDATE_SUMMARY = "Atualizar tópico (PUT parcial)";

        public static final String UPDATE_DESCRIPTION = """
                Resposta: apenas campos da tabela `topic` (sem `account_name` nem `topic_annotator_links`). \
                `last_updated_at` é preenchido neste fluxo.""";

        public static final String CLOSE_SUMMARY = "Encerrar tópico (OPEN → CLOSED)";

        public static final String AI_CONSOLIDATE_SUMMARY = "Enfileirar consolidação de notas com IA";

        public static final String AI_CONSOLIDATE_DESCRIPTION = """
                Resposta 202 Accepted. Exige `OWNER_FULL` (dono com tópico OPEN). Processamento assíncrono.""";

        public static final String AI_REPORTS_LIST_SUMMARY = "Listar relatórios IA do tópico";

        public static final String AI_REPORTS_PAGE_SUMMARY = "Listar relatórios IA da conta (paginado)";

        public static final String AI_LATEST_PAYLOAD_SUMMARY = "Último payload JSON bruto do relatório IA";

        public static final String BLOCK_ANNOTATOR_SUMMARY = "Bloquear anotador para o dono (via tópico)";

        public static final String LIST_BLOCKED_SUMMARY = "Listar anotadores bloqueados pelo dono";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Annotator {

        public static final String LIST_LINKS_SUMMARY = "Listar vínculos tópico–anotador (visão anotador)";

        public static final String LIST_LINKS_DESCRIPTION = """
                O `accountId` no path é o do anotador; exige papel `ANNOTATOR` no JWT.""";

        public static final String CREATE_LINK_SUMMARY = "Criar vínculo tópico–anotador";

        public static final String CREATE_LINK_DESCRIPTION = """
                Dono do tópico (`OWNER_*`) ou anotador (`ANNOTATOR`) conforme `requireAnnotatorLinkParticipant`.""";

        public static final String UPDATE_RESUME_SUMMARY = "Atualizar resumo do vínculo (anotador/dono)";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class TextAiAssistant {

        public static final String PROCESS_SUMMARY = "Colaborar em texto com assistente IA (thread OpenAI)";

        public static final String PROCESS_DESCRIPTION = """
                Requer perfil permitido em `TextAiAssistantAccessConstants` e `app.openai.assistant-text-ai-id` preenchido. \
                Corpo (request): chaves em snake_case (`thread_id`, etc.) com alias camelCase onde aplicável (`threadId`). \
                Primeira chamada: omita `thread_id` e envie `title`, `resume`, `time`, `expected` e `chat`. \
                Chamadas seguintes: envie o `thread_id` retornado; a sessão local já existe — se `is_deleted` for true (soft delete), a continuação falha antes de chamar a OpenAI. \
                `title`, `resume`, `time` e `expected` são opcionais na continuação e reutilizam a sessão persistida. \
                **Resposta deste controller:** JSON em **camelCase** (`threadId`, `allResume`, etc.). O payload interno ao modelo continua em camelCase. \
                Após a primeira volta, `lastUpdatedAt` na entidade só é preenchido nas continuações. \
                Se o modelo responder `modification=true`, o `resume` persistido é substituído por `allResume`.""";

        public static final String LIST_SESSIONS_SUMMARY = "Listar sessões de texto com IA da conta (paginado)";

        public static final String LIST_SESSIONS_DESCRIPTION = """
                Requer o mesmo perfil que o POST `text-ai-assistant` (`TextAiAssistantAccessConstants`). \
                Sessões soft-deleted não entram na lista. \
                Resposta JSON em camelCase (ex.: `openAiThreadId`, `totalElements`, `createdAt`). \
                Ordenação: `created_at` na base, descendente, depois `id`. Parâmetros `page` e `size` opcionais (mesmos limites da listagem de tópicos).""";

        public static final String GET_SESSION_BY_ID_SUMMARY = "Obter sessão de texto com IA por id (escopo da conta)";

        public static final String GET_SESSION_BY_ID_DESCRIPTION = """
                O `textAiSessionId` deve pertencer ao `accountId` do path, não estar removida logicamente e pertencer a uma conta ACTIVE; \
                caso contrário retorna 400. Corpo em camelCase (ex.: `openAiThreadId`, `lastUpdatedAt`).""";

        public static final String DELETE_SESSION_SUMMARY = "Excluir logicamente sessão de texto com IA (escopo da conta)";

        public static final String DELETE_SESSION_DESCRIPTION = """
                Requer o mesmo perfil e regras de conta ativa que os GETs de sessões. \
                Atualiza `last_updated_at`, define `is_deleted` e registra uma linha em `account_text_ai_session_history` \
                (não remove a linha principal). Repetir a exclusão ou consultar sessão já excluída responde como recurso inexistente para a conta.""";
    }
}
