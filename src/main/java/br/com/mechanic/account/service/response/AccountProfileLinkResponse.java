package br.com.mechanic.account.service.response;

import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountProfileLinkResponse {

    private Long accountId;
    private Long profileId;
    private AccountProfileTypeEnum profileType;
}
