package br.com.mechanic.account.service.request;

import br.com.mechanic.account.constant.AuthValidationConstants;
import br.com.mechanic.account.constant.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank
        @Pattern(
                regexp = AuthValidationConstants.LOGIN_EMAIL_REGEXP,
                message = AuthValidationConstants.MESSAGE_LOGIN_EMAIL_DOMAIN
        )
        @Size(max = ValidationConstants.EMAIL_MAX_LENGTH)
        String email,

        @NotBlank
        @Size(min = ValidationConstants.PASSWORD_MIN_LENGTH, max = ValidationConstants.PASSWORD_MAX_LENGTH)
        String password
) {
}
