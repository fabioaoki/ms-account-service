package br.com.mechanic.account.controller.account;

import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.service.account.AccountServiceBO;
import br.com.mechanic.account.service.request.AccountPresentationSummaryUpsertRequest;
import br.com.mechanic.account.service.request.AccountProfileLinkRequest;
import br.com.mechanic.account.service.request.AccountProfileUnlinkRequest;
import br.com.mechanic.account.service.request.AccountUpdateRequest;
import br.com.mechanic.account.service.request.UserCreateRequest;
import br.com.mechanic.account.service.response.AccountDetailResponse;
import br.com.mechanic.account.service.response.AccountPresentationSummaryResponse;
import br.com.mechanic.account.service.response.AccountProfileLinkResponse;
import br.com.mechanic.account.service.response.AccountUpdateResponse;
import br.com.mechanic.account.service.response.AccountResponse;
import br.com.mechanic.account.service.topic.AccountPresentationSummaryServiceBO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    private final AccountPresentationSummaryServiceBO accountPresentationSummaryServiceBO;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody UserCreateRequest request) {
        AccountResponse body = accountServiceBO.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping(ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AccountDetailResponse> getByAccountId(@PathVariable Long accountId) {
        AccountDetailResponse body = accountServiceBO.getByAccountId(accountId);
        return ResponseEntity.ok(body);
    }

    @PatchMapping(ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE + ApiPathConstants.ACCOUNT_DEACTIVATE_SEGMENT)
    @ResponseStatus(HttpStatus.OK)
    public void deactivateAccount(@PathVariable Long accountId) {
        accountServiceBO.deactivateAccount(accountId);
    }

    @PatchMapping(ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE + ApiPathConstants.ACCOUNT_ACTIVATE_SEGMENT)
    @ResponseStatus(HttpStatus.OK)
    public void activateAccount(@PathVariable Long accountId) {
        accountServiceBO.activateAccount(accountId);
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

    @PostMapping(ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE + ApiPathConstants.ACCOUNT_PROFILES_SEGMENT)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AccountProfileLinkResponse> linkProfile(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountProfileLinkRequest request
    ) {
        AccountProfileLinkResponse body = accountServiceBO.linkProfileToAccount(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @DeleteMapping(ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE + ApiPathConstants.ACCOUNT_PROFILES_SEGMENT)
    @ResponseStatus(HttpStatus.OK)
    public void unlinkProfile(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountProfileUnlinkRequest request
    ) {
        accountServiceBO.unlinkProfileFromAccount(accountId, request);
    }

    @PostMapping(
            ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE
                    + ApiPathConstants.ACCOUNT_PRESENTATION_SUMMARY_SEGMENT
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AccountPresentationSummaryResponse> createAccountPresentationSummary(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountPresentationSummaryUpsertRequest request
    ) {
        AccountPresentationSummaryResponse body = accountPresentationSummaryServiceBO.createSummary(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping(
            ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE
                    + ApiPathConstants.ACCOUNT_PRESENTATION_SUMMARY_SEGMENT
    )
    public ResponseEntity<AccountPresentationSummaryResponse> updateAccountPresentationSummary(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountPresentationSummaryUpsertRequest request
    ) {
        AccountPresentationSummaryResponse body = accountPresentationSummaryServiceBO.updateSummary(accountId, request);
        return ResponseEntity.ok(body);
    }

    @GetMapping(
            ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE
                    + ApiPathConstants.ACCOUNT_PRESENTATION_SUMMARY_SEGMENT
    )
    public ResponseEntity<AccountPresentationSummaryResponse> getAccountPresentationSummary(
            @PathVariable Long accountId
    ) {
        AccountPresentationSummaryResponse body = accountPresentationSummaryServiceBO.getSummary(accountId);
        return ResponseEntity.ok(body);
    }
}
