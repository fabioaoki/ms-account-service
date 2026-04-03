package br.com.mechanic.account.service.textai;

import br.com.mechanic.account.entity.textai.AccountTextAiSession;
import br.com.mechanic.account.service.response.AccountTextAiSessionResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountTextAiSessionMapper {

    public static AccountTextAiSessionResponse toResponse(AccountTextAiSession entity) {
        return new AccountTextAiSessionResponse(
                entity.getId(),
                entity.getAccount().getId(),
                entity.getOpenAiThreadId(),
                entity.getTitle(),
                entity.getResume(),
                entity.isTimeConsidered(),
                entity.getExpectedMinutes(),
                entity.getCreatedAt(),
                entity.getLastUpdatedAt()
        );
    }
}
