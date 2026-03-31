package br.com.mechanic.account.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normaliza texto incluído no JSON enviado ao modelo (ex.: {@code \r\n} → {@code \n}).
 * Sequências {@code \\uXXXX} no corpo JSON já são decodificadas pelo Jackson ao deserializar a requisição.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicAiPayloadTextNormalizer {

    private static final String CARRIAGE_RETURN_LINE_FEED = "\r\n";

    private static final String CARRIAGE_RETURN = "\r";

    private static final String LINE_FEED = "\n";

    private static final String EMPTY = "";

    private static final Pattern LEADING_ENUMERATION_PATTERN =
            Pattern.compile("^\\s*(?:\\d+[\\).:-]?|[\\-•*])\\s+");

    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile("\\s+");

    private static final Pattern EDGE_PUNCTUATION_PATTERN = Pattern.compile("^[\\p{Punct}\\s]+|[\\p{Punct}\\s]+$");

    /**
     * @param text texto armazenado; {@code null} permanece {@code null}
     */
    public static String normalizeMultilineForAiPayload(String text) {
        if (text == null) {
            return null;
        }
        String withUnixLineEndings = text.replace(CARRIAGE_RETURN_LINE_FEED, LINE_FEED);
        return withUnixLineEndings.replace(CARRIAGE_RETURN, LINE_FEED);
    }

    /**
     * Normalização canônica para comparação de equivalência textual no pós-processamento da resposta da IA.
     * Remove enumeração inicial (ex.: "1. "), compacta espaços e ignora pontuação de borda.
     */
    public static String normalizeForGrouping(String text) {
        if (text == null) {
            return EMPTY;
        }
        String withoutLineEndingNoise = normalizeMultilineForAiPayload(text).trim();
        String withoutLeadingEnumeration = LEADING_ENUMERATION_PATTERN
                .matcher(withoutLineEndingNoise)
                .replaceFirst(EMPTY);
        String singleSpaced = MULTIPLE_SPACES_PATTERN.matcher(withoutLeadingEnumeration).replaceAll(" ").trim();
        String withoutEdgePunctuation = EDGE_PUNCTUATION_PATTERN.matcher(singleSpaced).replaceAll(EMPTY).trim();
        return withoutEdgePunctuation.toLowerCase(Locale.ROOT);
    }
}
