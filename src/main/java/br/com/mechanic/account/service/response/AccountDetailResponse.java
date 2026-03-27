package br.com.mechanic.account.service.response;

import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.AccountStatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AccountDetailResponse(
        Long accountId,
        String name,
        LocalDate birthDate,
        LocalDateTime createdAt,
        AccountStatusEnum status,
        List<AccountProfileTypeEnum> profileTypes
) {
}
