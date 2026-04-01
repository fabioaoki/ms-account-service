package br.com.mechanic.account.service.request;

import br.com.mechanic.account.constant.AuthJsonConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank
        @JsonProperty(AuthJsonConstants.REFRESH_TOKEN)
        String refreshToken
) {
}
