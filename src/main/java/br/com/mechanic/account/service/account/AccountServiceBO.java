package br.com.mechanic.account.service.account;

import br.com.mechanic.account.service.request.AccountProfileLinkRequest;
import br.com.mechanic.account.service.request.AccountProfileUnlinkRequest;
import br.com.mechanic.account.service.request.UserCreateRequest;
import br.com.mechanic.account.service.response.AccountDetailResponse;
import br.com.mechanic.account.service.response.AccountProfileLinkResponse;
import br.com.mechanic.account.service.response.AccountResponse;
import br.com.mechanic.account.service.response.AccountUpdateResponse;
import br.com.mechanic.account.service.request.AccountUpdateRequest;

public interface AccountServiceBO {

    AccountResponse create(UserCreateRequest request);

    AccountUpdateResponse update(Long accountId, AccountUpdateRequest request);

    AccountProfileLinkResponse linkProfileToAccount(Long accountId, AccountProfileLinkRequest request);

    void unlinkProfileFromAccount(Long accountId, AccountProfileUnlinkRequest request);

    AccountDetailResponse getByAccountId(Long accountId);

    void deactivateAccount(Long accountId);

    void activateAccount(Long accountId);
}
