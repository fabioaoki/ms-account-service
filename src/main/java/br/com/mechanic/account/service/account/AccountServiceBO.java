package br.com.mechanic.account.service.account;

import br.com.mechanic.account.service.request.UserCreateRequest;
import br.com.mechanic.account.service.response.AccountResponse;
import br.com.mechanic.account.service.response.AccountUpdateResponse;
import br.com.mechanic.account.service.request.AccountUpdateRequest;

public interface AccountServiceBO {

    AccountResponse create(UserCreateRequest request);

    AccountUpdateResponse update(Long accountId, AccountUpdateRequest request);
}
