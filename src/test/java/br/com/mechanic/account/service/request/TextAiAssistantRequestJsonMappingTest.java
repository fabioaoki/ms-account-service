package br.com.mechanic.account.service.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TextAiAssistantRequestJsonMappingTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Deserializa resumeModification em camelCase para o campo resume_modification")
    void deserializesResumeModificationCamelCaseAlias() throws Exception {
        String json = """
                {
                  "threadId": "thread_abc",
                  "resumeModification": "(Romanos 15:13).",
                  "chat": "remova"
                }
                """;

        TextAiAssistantRequest request = objectMapper.readValue(json, TextAiAssistantRequest.class);

        assertEquals("thread_abc", request.threadId());
        assertEquals("(Romanos 15:13).", request.resumeModification());
        assertEquals("remova", request.chat());
        assertNull(request.title());
    }

    @Test
    @DisplayName("Deserializa resume_modification em snake_case")
    void deserializesResumeModificationSnakeCase() throws Exception {
        String json = """
                {
                  "thread_id": "thread_xyz",
                  "resume_modification": "trecho",
                  "chat": "ok"
                }
                """;

        TextAiAssistantRequest request = objectMapper.readValue(json, TextAiAssistantRequest.class);

        assertEquals("thread_xyz", request.threadId());
        assertEquals("trecho", request.resumeModification());
    }
}
