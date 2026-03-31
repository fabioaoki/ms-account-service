package br.com.mechanic.account.config;

import br.com.mechanic.account.constant.OpenAiApiConstants;
import br.com.mechanic.account.constant.TopicAiConsolidationPromptDefaults;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração da integração OpenAI ({@code app.openai.*}).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.openai")
public class OpenAiProperties {

    private boolean enabled;

    private String apiKey = "";

    private String model = "gpt-4o-mini";

    private String baseUrl = "https://api.openai.com";

    /**
     * Se preenchido (ex.: {@code asst_...}), usa a API de Assistants em vez de chat completions.
     * Instruções do assistente vêm do painel OpenAI; o prompt de sistema da app é ignorado nesse modo.
     */
    private String assistantId = "";

    /**
     * Assistente de revisão/aceitação da saída do assistente principal.
     * Mantido como "linguage" para compatibilidade com variável de ambiente existente.
     */
    private String assistantLinguageId = "";

    private String consolidationSystemPrompt = TopicAiConsolidationPromptDefaults.DEFAULT_SYSTEM_PROMPT;

    /**
     * URL base sem sufixo {@value br.com.mechanic.account.constant.OpenAiApiConstants#API_VERSION_PATH_SUFFIX}
     * duplicado (compatível com {@code https://api.openai.com} ou {@code https://api.openai.com/v1}).
     */
    public String getResolvedBaseUrlHost() {
        String url = baseUrl == null ? "" : baseUrl.trim();
        if (url.isEmpty()) {
            return OpenAiApiConstants.DEFAULT_RESOLVED_BASE_URL;
        }
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (url.endsWith(OpenAiApiConstants.API_VERSION_PATH_SUFFIX)) {
            url = url.substring(0, url.length() - OpenAiApiConstants.API_VERSION_PATH_SUFFIX.length());
        }
        return url;
    }

    public boolean usesAssistant() {
        return assistantId != null && !assistantId.isBlank();
    }

    public boolean usesAssistantLinguage() {
        return assistantLinguageId != null && !assistantLinguageId.isBlank();
    }

    /**
     * Valor gravado em {@code topic_ai_report.openai_model}: modelo de chat ou id do assistente.
     */
    public String resolvePersistedModelLabel() {
        if (usesAssistant()) {
            return assistantId.trim();
        }
        return model == null ? "" : model;
    }
}
