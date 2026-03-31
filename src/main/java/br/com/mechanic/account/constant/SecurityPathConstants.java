package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityPathConstants {

    /**
     * Entrada configurada em {@code springdoc.swagger-ui.path} (ex.: {@code /swagger-ui.html}).
     * Não é coberta por {@link #SWAGGER_UI_PATTERN} ({@code /swagger-ui/**}).
     */
    public static final String SWAGGER_UI_HTML_PATH = "/swagger-ui.html";

    public static final String SWAGGER_UI_PATTERN = "/swagger-ui/**";

    public static final String API_DOCS_PATTERN = "/v3/api-docs/**";

    /** Recursos estáticos do Swagger UI (springdoc / webjars). */
    public static final String WEBJARS_PATTERN = "/webjars/**";

    public static final String ACTUATOR_HEALTH_PATTERN = "/actuator/health";
}
