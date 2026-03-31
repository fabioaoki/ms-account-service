package br.com.mechanic.account.service.response;

import br.com.mechanic.account.constant.AuthJsonConstants;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LoginResponse(
        @JsonProperty(AuthJsonConstants.ACCESS_TOKEN)
        String accessToken,

        @JsonProperty(AuthJsonConstants.TOKEN_TYPE)
        String tokenType,

        @JsonProperty(AuthJsonConstants.EXPIRES_IN_SECONDS)
        long expiresInSeconds,

        @JsonProperty(AuthJsonConstants.AUTHORITIES)
        List<String> authorities
) {
}
