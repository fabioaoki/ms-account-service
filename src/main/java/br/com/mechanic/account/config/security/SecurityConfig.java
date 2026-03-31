package br.com.mechanic.account.config.security;

import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.CryptConstants;
import br.com.mechanic.account.constant.SecurityPathConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, ApiPathConstants.ACCOUNTS_BASE_PATH).permitAll()
                        .requestMatchers(HttpMethod.POST, ApiPathConstants.ACCOUNTS_ACCOUNT_ID_PROFILES_ANT_PATTERN)
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, ApiPathConstants.ACCOUNTS_ACCOUNT_ID_TOPICS_ANT_PATTERN)
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                ApiPathConstants.ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_ANNOTATOR_LINK_ANT_PATTERN
                        )
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.PUT,
                                ApiPathConstants.ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_ANNOTATOR_LINK_RESUME_ANT_PATTERN
                        )
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                ApiPathConstants.ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_ANNOTATOR_BLOCKS_ANT_PATTERN
                        )
                        .permitAll()
                        .requestMatchers(HttpMethod.PUT, ApiPathConstants.ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_ANT_PATTERN)
                        .permitAll()
                        .requestMatchers(HttpMethod.PATCH, ApiPathConstants.ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_CLOSE_ANT_PATTERN)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, ApiPathConstants.ACCOUNTS_ACCOUNT_ID_TOPICS_ANT_PATTERN)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, ApiPathConstants.ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_ANT_PATTERN)
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                ApiPathConstants.ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_AI_CONSOLIDATION_ANT_PATTERN
                        )
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                ApiPathConstants.ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_AI_REPORTS_ANT_PATTERN
                        )
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                ApiPathConstants.ACCOUNTS_ACCOUNT_ID_TOPICS_TOPIC_ID_AI_REPORT_LATEST_RESPONSE_ANT_PATTERN
                        )
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                ApiPathConstants.ACCOUNTS_ACCOUNT_ID_TOPIC_AI_REPORTS_ANT_PATTERN
                        )
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                ApiPathConstants.ACCOUNTS_ACCOUNT_ID_TOPIC_ANNOTATOR_LINKS_ANT_PATTERN
                        )
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                ApiPathConstants.ACCOUNTS_ACCOUNT_ID_ANNOTATOR_BLOCKS_ANT_PATTERN
                        )
                        .permitAll()
                        .requestMatchers(HttpMethod.DELETE, ApiPathConstants.ACCOUNTS_ACCOUNT_ID_PROFILES_ANT_PATTERN)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, ApiPathConstants.ACCOUNTS_ACCOUNT_ID_GET_ANT_PATTERN)
                        .permitAll()
                        .requestMatchers(HttpMethod.PATCH, ApiPathConstants.ACCOUNTS_WITH_ID_WILDCARD_PATH).permitAll()
                        .requestMatchers(
                                SecurityPathConstants.SWAGGER_UI_PATTERN,
                                SecurityPathConstants.API_DOCS_PATTERN,
                                SecurityPathConstants.ACTUATOR_HEALTH_PATTERN
                        ).permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(CryptConstants.BCRYPT_STRENGTH);
    }
}
