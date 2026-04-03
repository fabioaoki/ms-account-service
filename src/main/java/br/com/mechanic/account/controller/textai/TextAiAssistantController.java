package br.com.mechanic.account.controller.textai;

import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.OpenApiOperationDocumentationConstants;
import br.com.mechanic.account.constant.TextAiSessionListQueryConstants;
import br.com.mechanic.account.service.request.TextAiAssistantRequest;
import br.com.mechanic.account.service.response.AccountTextAiSessionPageResponse;
import br.com.mechanic.account.service.response.AccountTextAiSessionResponse;
import br.com.mechanic.account.service.response.TextAiAssistantResponse;
import br.com.mechanic.account.service.textai.AccountTextAiSessionDeletionServiceBO;
import br.com.mechanic.account.service.textai.AccountTextAiSessionQueryServiceBO;
import br.com.mechanic.account.service.textai.TextAiAssistantServiceBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.ACCOUNTS_BASE_PATH)
@Tag(
        name = OpenApiOperationDocumentationConstants.Tag.TEXT_AI_ASSISTANT_NAME,
        description = OpenApiOperationDocumentationConstants.Tag.TEXT_AI_ASSISTANT_DESCRIPTION
)
public class TextAiAssistantController {

    private final TextAiAssistantServiceBO textAiAssistantService;
    private final AccountTextAiSessionQueryServiceBO accountTextAiSessionQueryService;
    private final AccountTextAiSessionDeletionServiceBO accountTextAiSessionDeletionService;

    @GetMapping(ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE + ApiPathConstants.TEXT_AI_ASSISTANT_SESSIONS_SEGMENT)
    @Operation(
            summary = OpenApiOperationDocumentationConstants.TextAiAssistant.LIST_SESSIONS_SUMMARY,
            description = OpenApiOperationDocumentationConstants.TextAiAssistant.LIST_SESSIONS_DESCRIPTION
    )
    public ResponseEntity<AccountTextAiSessionPageResponse> listSessions(
            @PathVariable Long accountId,
            @RequestParam(name = TextAiSessionListQueryConstants.PAGE, required = false) Integer page,
            @RequestParam(name = TextAiSessionListQueryConstants.SIZE, required = false) Integer size
    ) {
        return ResponseEntity.ok(accountTextAiSessionQueryService.listByAccountId(accountId, page, size));
    }

    @GetMapping(
            ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE
                    + ApiPathConstants.TEXT_AI_ASSISTANT_SESSIONS_SEGMENT
                    + ApiPathConstants.TEXT_AI_SESSION_ID_PATH_VARIABLE
    )
    @Operation(
            summary = OpenApiOperationDocumentationConstants.TextAiAssistant.GET_SESSION_BY_ID_SUMMARY,
            description = OpenApiOperationDocumentationConstants.TextAiAssistant.GET_SESSION_BY_ID_DESCRIPTION
    )
    public ResponseEntity<AccountTextAiSessionResponse> getSessionById(
            @PathVariable Long accountId,
            @PathVariable Long textAiSessionId
    ) {
        return ResponseEntity.ok(accountTextAiSessionQueryService.getByIdAndAccountId(accountId, textAiSessionId));
    }

    @DeleteMapping(
            ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE
                    + ApiPathConstants.TEXT_AI_ASSISTANT_SESSIONS_SEGMENT
                    + ApiPathConstants.TEXT_AI_SESSION_ID_PATH_VARIABLE
    )
    @Operation(
            summary = OpenApiOperationDocumentationConstants.TextAiAssistant.DELETE_SESSION_SUMMARY,
            description = OpenApiOperationDocumentationConstants.TextAiAssistant.DELETE_SESSION_DESCRIPTION
    )
    public ResponseEntity<Void> deleteSession(
            @PathVariable Long accountId,
            @PathVariable Long textAiSessionId
    ) {
        accountTextAiSessionDeletionService.softDeleteByAccountIdAndSessionId(accountId, textAiSessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE + ApiPathConstants.TEXT_AI_ASSISTANT_SEGMENT)
    @Operation(
            summary = OpenApiOperationDocumentationConstants.TextAiAssistant.PROCESS_SUMMARY,
            description = OpenApiOperationDocumentationConstants.TextAiAssistant.PROCESS_DESCRIPTION
    )
    public ResponseEntity<TextAiAssistantResponse> process(
            @PathVariable Long accountId,
            @Valid @RequestBody TextAiAssistantRequest request
    ) {
        return ResponseEntity.ok(textAiAssistantService.process(accountId, request));
    }
}
