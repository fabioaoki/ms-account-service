package br.com.mechanic.account.controller.auth;

import br.com.mechanic.account.annotation.PublicEndpoint;
import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.OpenApiOperationDocumentationConstants;
import br.com.mechanic.account.service.auth.AuthServiceBO;
import br.com.mechanic.account.service.request.LoginRequest;
import br.com.mechanic.account.service.request.RefreshTokenRequest;
import br.com.mechanic.account.service.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.AUTH_BASE_PATH)
@Tag(
        name = OpenApiOperationDocumentationConstants.Tag.AUTH_NAME,
        description = OpenApiOperationDocumentationConstants.Tag.AUTH_DESCRIPTION
)
public class AuthController {

    private final AuthServiceBO authServiceBO;

    @PublicEndpoint
    @Operation(
            summary = OpenApiOperationDocumentationConstants.Auth.LOGIN_SUMMARY,
            description = OpenApiOperationDocumentationConstants.Auth.LOGIN_DESCRIPTION
    )
    @PostMapping(ApiPathConstants.AUTH_LOGIN_SEGMENT)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authServiceBO.login(request));
    }

    @PublicEndpoint
    @Operation(
            summary = OpenApiOperationDocumentationConstants.Auth.REFRESH_SUMMARY,
            description = OpenApiOperationDocumentationConstants.Auth.REFRESH_DESCRIPTION
    )
    @PostMapping(ApiPathConstants.AUTH_REFRESH_SEGMENT)
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authServiceBO.refresh(request));
    }
}
