package br.com.mechanic.account.config.security;

import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.CryptConstants;
import br.com.mechanic.account.constant.SecurityPathConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            @Value("${app.security.integration-test-disable-jwt:false}") boolean integrationTestDisableJwt
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        if (integrationTestDisableJwt) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, ApiPathConstants.ACCOUNTS_BASE_PATH).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                ApiPathConstants.AUTH_BASE_PATH + ApiPathConstants.AUTH_LOGIN_SEGMENT
                        )
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                ApiPathConstants.AUTH_BASE_PATH + ApiPathConstants.AUTH_REFRESH_SEGMENT
                        )
                        .permitAll()
                        .requestMatchers(
                                SecurityPathConstants.SWAGGER_UI_HTML_PATH,
                                SecurityPathConstants.SWAGGER_UI_PATTERN,
                                SecurityPathConstants.API_DOCS_PATTERN,
                                SecurityPathConstants.WEBJARS_PATTERN,
                                SecurityPathConstants.ACTUATOR_HEALTH_PATTERN
                        )
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                ));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(CryptConstants.BCRYPT_STRENGTH);
    }
}
