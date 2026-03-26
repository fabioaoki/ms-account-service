package br.com.mechanic.account.service.request;

import br.com.mechanic.account.constant.ValidationConstants;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AccountUpdateRequest(

        @Size(max = ValidationConstants.FIRST_NAME_MAX_LENGTH)
        String firstName,

        @Size(max = ValidationConstants.LAST_NAME_MAX_LENGTH)
        String lastName,

        LocalDate birthDate
) {
}

