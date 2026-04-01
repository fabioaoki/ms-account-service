package br.com.mechanic.account.config;

import br.com.mechanic.account.annotation.PublicEndpoint;
import br.com.mechanic.account.constant.AuthJwtConstants;
import br.com.mechanic.account.constant.OpenApiDocumentationConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI msAccountOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title(OpenApiDocumentationConstants.API_TITLE)
                        .version(OpenApiDocumentationConstants.API_VERSION)
                        .description(buildApiDescription()))
                .components(new Components()
                        .addSecuritySchemes(
                                OpenApiDocumentationConstants.BEARER_JWT_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme(OpenApiDocumentationConstants.HTTP_SCHEME_BEARER)
                                        .bearerFormat(OpenApiDocumentationConstants.BEARER_FORMAT_JWT)
                                        .description(OpenApiDocumentationConstants.BEARER_JWT_SCHEME_DESCRIPTION)
                        ));
    }

    /**
     * Aplica segurança Bearer a todas as operações, exceto as marcadas com {@link PublicEndpoint}.
     */
    @Bean
    public OperationCustomizer bearerJwtOperationCustomizer() {
        return (operation, handlerMethod) -> {
            if (isPublicEndpoint(handlerMethod)) {
                return operation;
            }
            operation.addSecurityItem(new SecurityRequirement()
                    .addList(OpenApiDocumentationConstants.BEARER_JWT_SCHEME_NAME));
            return operation;
        };
    }

    private static boolean isPublicEndpoint(HandlerMethod handlerMethod) {
        return handlerMethod != null && handlerMethod.hasMethodAnnotation(PublicEndpoint.class);
    }

    private static String buildApiDescription() {
        long minutesAccess =
                AuthJwtConstants.DEFAULT_ACCESS_TOKEN_EXPIRATION_SECONDS / AuthJwtConstants.SECONDS_PER_MINUTE;
        long daysRefresh =
                AuthJwtConstants.DEFAULT_REFRESH_TOKEN_EXPIRATION_SECONDS / AuthJwtConstants.SECONDS_PER_DAY;
        return OpenApiDocumentationConstants.API_DESCRIPTION
                + "\n\n**TTL:** access token ~"
                + minutesAccess
                + " min (`expires_in_seconds`); refresh ~"
                + daysRefresh
                + " dias (`refresh_expires_in_seconds`). "
                + "`app.security.jwt.access-token-expiration-seconds` / `refresh-token-expiration-seconds`.\n";
    }
}
