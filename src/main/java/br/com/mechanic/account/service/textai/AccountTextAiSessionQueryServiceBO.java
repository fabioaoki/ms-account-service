package br.com.mechanic.account.service.textai;

import br.com.mechanic.account.service.response.AccountTextAiSessionPageResponse;
import br.com.mechanic.account.service.response.AccountTextAiSessionResponse;

public interface AccountTextAiSessionQueryServiceBO {

    AccountTextAiSessionPageResponse listByAccountId(Long accountId, Integer page, Integer size);

    AccountTextAiSessionResponse getByIdAndAccountId(Long accountId, Long textAiSessionId);
}
