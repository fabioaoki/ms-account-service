package br.com.mechanic.account.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TopicAiPayloadTextNormalizerTest {

    @Test
    @DisplayName("normalizeForGrouping remove enumeração e iguala conteúdo equivalente")
    void normalizeForGroupingShouldIgnoreLeadingEnumerationAndSpacing() {
        String base = "Perdoar os pais é um passo essencial para a cura interior.";
        String enumerated = "1.   Perdoar os pais é um passo essencial para a cura interior. ";
        String dashed = "- Perdoar os pais é um passo essencial para a cura interior";

        String normalizedBase = TopicAiPayloadTextNormalizer.normalizeForGrouping(base);

        assertEquals(normalizedBase, TopicAiPayloadTextNormalizer.normalizeForGrouping(enumerated));
        assertEquals(normalizedBase, TopicAiPayloadTextNormalizer.normalizeForGrouping(dashed));
    }

    @Test
    void normalizeReplacesCrLfAndLonelyCrWithLf() {
        assertEquals("a\nb\nc", TopicAiPayloadTextNormalizer.normalizeMultilineForAiPayload("a\r\nb\rc"));
    }

    @Test
    void normalizeNullReturnsNull() {
        assertNull(TopicAiPayloadTextNormalizer.normalizeMultilineForAiPayload(null));
    }
}
