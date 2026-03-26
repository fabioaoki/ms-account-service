package br.com.mechanic.account.controller.account;

import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.service.account.AccountServiceBO;
import br.com.mechanic.account.service.request.AccountUpdateRequest;
import br.com.mechanic.account.service.request.UserCreateRequest;
import br.com.mechanic.account.service.response.AccountUpdateResponse;
import br.com.mechanic.account.service.response.AccountResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.ACCOUNTS_BASE_PATH)
public class AccountController {

    private final AccountServiceBO accountServiceBO;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody UserCreateRequest request) {
        AccountResponse body = accountServiceBO.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PatchMapping(ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AccountUpdateResponse> update(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountUpdateRequest request
    ) {
        AccountUpdateResponse body = accountServiceBO.update(accountId, request);
        return ResponseEntity.ok(body);
    }
}
