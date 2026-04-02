package br.com.mechanic.account.controller.textai;

import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.OpenApiOperationDocumentationConstants;
import br.com.mechanic.account.service.request.TextAiAssistantRequest;
import br.com.mechanic.account.service.response.TextAiAssistantResponse;
import br.com.mechanic.account.service.textai.TextAiAssistantServiceBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
