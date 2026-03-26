package br.com.mechanic.account.service.request;

import br.com.mechanic.account.constant.AuthValidationConstants;
import br.com.mechanic.account.constant.ValidationConstants;
import br.com.mechanic.account.validation.PasswordConfirmationMatches;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@PasswordConfirmationMatches
public record UserCreateRequest(
        @NotBlank
        @Pattern(
                regexp = AuthValidationConstants.LOGIN_EMAIL_REGEXP,
                message = AuthValidationConstants.MESSAGE_LOGIN_EMAIL_DOMAIN
        )
        @Size(max = ValidationConstants.EMAIL_MAX_LENGTH)
        String email,
        @NotBlank
        @Size(min = ValidationConstants.PASSWORD_MIN_LENGTH, max = ValidationConstants.PASSWORD_MAX_LENGTH)
        String password,
        @NotBlank
        @Size(min = ValidationConstants.PASSWORD_MIN_LENGTH, max = ValidationConstants.PASSWORD_MAX_LENGTH)
        String confirmPassword,
        @NotBlank
        @Size(min = ValidationConstants.NAME_MIN_LENGTH, max = ValidationConstants.FIRST_NAME_MAX_LENGTH)
        String firstName,
        @NotBlank
        @Size(min = ValidationConstants.NAME_MIN_LENGTH, max = ValidationConstants.LAST_NAME_MAX_LENGTH)
        String lastName,
        @NotNull
        LocalDate birthDate
) {
}
