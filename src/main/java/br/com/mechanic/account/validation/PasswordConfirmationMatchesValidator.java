package br.com.mechanic.account.validation;

import br.com.mechanic.account.service.request.UserCreateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class PasswordConfirmationMatchesValidator
        implements ConstraintValidator<PasswordConfirmationMatches, UserCreateRequest> {

    @Override
    public boolean isValid(UserCreateRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return Objects.equals(value.password(), value.confirmPassword());
    }
}
