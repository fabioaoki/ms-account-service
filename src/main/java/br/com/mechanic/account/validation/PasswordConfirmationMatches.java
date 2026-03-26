package br.com.mechanic.account.validation;

import br.com.mechanic.account.constant.AccountRegistrationValidationConstants;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordConfirmationMatchesValidator.class)
public @interface PasswordConfirmationMatches {

    String message() default AccountRegistrationValidationConstants.MESSAGE_PASSWORD_AND_CONFIRMATION_MUST_MATCH;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
