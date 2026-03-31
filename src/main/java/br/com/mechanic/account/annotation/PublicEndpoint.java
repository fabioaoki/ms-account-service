package br.com.mechanic.account.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca operações expostas sem autenticação JWT (alinhado a {@code permitAll} no {@code SecurityConfig}).
 * Usado pelo customizador OpenAPI para não exigir o scheme {@code bearer-jwt} no Swagger.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PublicEndpoint {
}
