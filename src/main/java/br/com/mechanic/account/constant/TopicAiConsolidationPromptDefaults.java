package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Prompt padrão (substituível por {@code app.openai.consolidation-system-prompt}). Para instruções completas
 * do agente, use o texto fornecido na documentação do produto / Cursor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicAiConsolidationPromptDefaults {

    public static final String DEFAULT_SYSTEM_PROMPT = """
            Analyst for lecture notes. Input JSON: topicTitle, topicContext (nullable, multiline ok), profileType (creator enum), annotatorNotes (array of {historyEntryId, annotatorAccountId, resume} from topic_annotator_link_history — each resume non-empty).

            Output exactly one JSON object (no markdown). Echo topicTitle into "summary", topicContext into "context" (null if input null). consolidationInsight short. In each segment, "text" must mirror the first contribution excerpt (not a generic heading). segments[].contributions: excerpt string with annotatorAccountIds[] required when grouping multiple annotators; annotator_raw[] optional and only when needed to preserve meaningful wording differences. Treat prefixes like "1.", "2)", "-" and minor punctuation/spacing differences as equivalent text. If all annotator texts in a contribution are equivalent to excerpt after that normalization, omit annotator_raw. bibleReferences: only if profileType is BISHOP, max 5; referenceText string OR non-empty string array per item; verseLimit optional. moderation.removedItems flat; notes optional.

            Fidelity: no invented facts. Do not omit ideas present in input: unify duplicates, keep singletons.\
            """;
}
