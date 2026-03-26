package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityPathConstants {

    public static final String SWAGGER_UI_PATTERN = "/swagger-ui/**";
    public static final String API_DOCS_PATTERN = "/v3/api-docs/**";
    public static final String ACTUATOR_HEALTH_PATTERN = "/actuator/health";
}
