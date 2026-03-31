package br.com.mechanic.account.service.topic;

import br.com.mechanic.account.service.request.AccountPresentationSummaryUpsertRequest;
import br.com.mechanic.account.service.response.AccountPresentationSummaryResponse;

public interface AccountPresentationSummaryServiceBO {

    AccountPresentationSummaryResponse createSummary(Long accountId, AccountPresentationSummaryUpsertRequest request);

    AccountPresentationSummaryResponse updateSummary(Long accountId, AccountPresentationSummaryUpsertRequest request);

    AccountPresentationSummaryResponse getSummary(Long accountId);
}
