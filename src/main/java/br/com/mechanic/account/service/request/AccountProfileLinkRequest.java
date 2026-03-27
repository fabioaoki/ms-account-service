package br.com.mechanic.account.service.request;

import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import jakarta.validation.constraints.NotNull;

public record AccountProfileLinkRequest(@NotNull AccountProfileTypeEnum profileType) {
}
