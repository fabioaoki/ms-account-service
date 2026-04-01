package br.com.mechanic.account.service.auth;

import br.com.mechanic.account.service.request.LoginRequest;
import br.com.mechanic.account.service.request.RefreshTokenRequest;
import br.com.mechanic.account.service.response.LoginResponse;

public interface AuthServiceBO {

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);
}
