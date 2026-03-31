package br.com.mechanic.account.config;

import br.com.mechanic.account.service.openai.DisabledOpenAiChatCompletionClient;
import br.com.mechanic.account.service.openai.DisabledOpenAiAssistantReviewClient;
import br.com.mechanic.account.service.openai.OpenAiAssistantCompletionClient;
import br.com.mechanic.account.service.openai.OpenAiAssistantReviewClient;
import br.com.mechanic.account.service.openai.OpenAiAssistantReviewPort;
import br.com.mechanic.account.service.openai.OpenAiChatCompletionClient;
import br.com.mechanic.account.service.openai.OpenAiChatCompletionPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiClientConfiguration {

    @Bean
    public RestClient openAiRestClient(OpenAiProperties properties) {
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getResolvedBaseUrlHost());
        if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey());
        }
        return builder.build();
    }

    @Bean
    public OpenAiChatCompletionPort openAiChatCompletionPort(
            RestClient openAiRestClient,
            OpenAiProperties properties,
            ObjectMapper objectMapper
    ) {
        if (!properties.isEnabled()) {
            return new DisabledOpenAiChatCompletionClient();
        }
        if (properties.usesAssistant()) {
            return new OpenAiAssistantCompletionClient(properties, objectMapper);
        }
        return new OpenAiChatCompletionClient(openAiRestClient, properties, objectMapper);
    }

    @Bean
    public OpenAiAssistantReviewPort openAiAssistantReviewPort(
            OpenAiProperties properties,
            ObjectMapper objectMapper
    ) {
        if (!properties.isEnabled() || !properties.usesAssistantLinguage()) {
            return new DisabledOpenAiAssistantReviewClient();
        }
        return new OpenAiAssistantReviewClient(properties, objectMapper);
    }
}
