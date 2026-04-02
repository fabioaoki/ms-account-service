package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextAiAssistantValidationConstants {

    public static final int MIN_EXPECTED_MINUTES = 1;

    public static final int MAX_EXPECTED_MINUTES =
            TimeDurationConstants.HOURS_PER_DAY * TimeDurationConstants.MINUTES_PER_HOUR;

    public static final String MESSAGE_SESSION_NOT_FOUND_FOR_THREAD = "Sessão não encontrada para o thread_id informado.";

    public static final String MESSAGE_ASSISTANT_TEXT_AI_NOT_CONFIGURED = "Assistente OpenAI para colaboração de texto não configurado.";

    public static final String MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON = "Resposta do assistente não é um JSON válido no formato esperado.";

    public static final String MESSAGE_FIRST_TURN_TITLE_REQUIRED =
            "Na primeira interação (sem thread_id), o campo title é obrigatório.";

    public static final String MESSAGE_FIRST_TURN_RESUME_REQUIRED =
            "Na primeira interação (sem thread_id), o campo resume é obrigatório.";

    public static final String MESSAGE_FIRST_TURN_TIME_REQUIRED =
            "Na primeira interação (sem thread_id), o campo time é obrigatório (true ou false).";

    public static final String MESSAGE_FIRST_TURN_EXPECTED_INVALID =
            "Na primeira interação (sem thread_id), o campo expected é obrigatório e deve ser um número inteiro de minutos dentro do intervalo permitido.";

    public static final String MESSAGE_EXPECTED_MINUTES_OUT_OF_RANGE_WHEN_PROVIDED =
            "Quando informado, expected deve ser um número inteiro de minutos dentro do intervalo permitido.";
}
