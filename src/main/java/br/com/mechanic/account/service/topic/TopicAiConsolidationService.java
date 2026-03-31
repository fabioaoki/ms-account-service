package br.com.mechanic.account.service.topic;

import br.com.mechanic.account.config.OpenAiProperties;
import br.com.mechanic.account.constant.AccountUpdateValidationConstants;
import br.com.mechanic.account.constant.TopicAiConsolidationLogConstants;
import br.com.mechanic.account.constant.TopicAiModelResponseJsonConstants;
import br.com.mechanic.account.constant.TopicAiReviewConstants;
import br.com.mechanic.account.constant.TopicAiUserPayloadJsonConstants;
import br.com.mechanic.account.constant.TopicPaginationConstants;
import br.com.mechanic.account.constant.TopicServiceLogConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.topic.TopicAiProcessingError;
import br.com.mechanic.account.entity.topic.Topic;
import br.com.mechanic.account.entity.topic.TopicAiReport;
import br.com.mechanic.account.entity.topic.TopicAnnotatorLinkHistory;
import br.com.mechanic.account.entity.topic.TopicHistory;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import br.com.mechanic.account.exception.AccountException;
import br.com.mechanic.account.mapper.topic.TopicAiReportMapper;
import br.com.mechanic.account.repository.account.impl.AccountRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.TopicAiProcessingErrorRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.TopicAiReportRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.TopicAnnotatorLinkHistoryRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.TopicHistoryRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.TopicRepositoryImpl;
import br.com.mechanic.account.service.openai.OpenAiAssistantReviewPort;
import br.com.mechanic.account.service.openai.OpenAiAssistantReviewResult;
import br.com.mechanic.account.service.openai.OpenAiChatCompletionPort;
import br.com.mechanic.account.service.response.TopicAiReportPageResponse;
import br.com.mechanic.account.service.response.TopicAiReportResponse;
import br.com.mechanic.account.util.TopicAiPayloadTextNormalizer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
@Slf4j
@RequiredArgsConstructor
public class TopicAiConsolidationService implements TopicAiConsolidationServiceBO {

    private static final int FIRST_ARRAY_INDEX = 0;

    private final AccountRepositoryImpl accountRepository;
    private final TopicRepositoryImpl topicRepository;
    private final TopicAnnotatorLinkHistoryRepositoryImpl topicAnnotatorLinkHistoryRepository;
    private final TopicAiReportRepositoryImpl topicAiReportRepository;
    private final TopicAiProcessingErrorRepositoryImpl topicAiProcessingErrorRepository;
    private final TopicHistoryRepositoryImpl topicHistoryRepository;
    private final OpenAiChatCompletionPort openAiChatCompletionPort;
    private final OpenAiAssistantReviewPort openAiAssistantReviewPort;
    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void enqueueReportGeneration(Long accountId, Long topicId) {
        log.info(TopicAiConsolidationLogConstants.CONSOLIDATION_FLOW_STARTED, accountId, topicId);
        if (!openAiProperties.isEnabled()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_NOT_ENABLED);
        }
        if (openAiProperties.getApiKey() == null || openAiProperties.getApiKey().isBlank()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_API_KEY_NOT_CONFIGURED);
        }
        if (!openAiProperties.usesAssistantLinguage()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_REVIEW_ASSISTANT_NOT_CONFIGURED);
        }

        getAccountOrThrowAndAssertActive(accountId);
        Topic topic = getTopicOwnedByAccountOrThrow(topicId, accountId);

        assertTopicEligibleForAiConsolidation(topic);

        List<TopicAnnotatorLinkHistory> historyRows =
                topicAnnotatorLinkHistoryRepository.findAllWithNonBlankResumeByTopicIdOrderByCreatedAtAsc(topicId);
        if (historyRows.isEmpty()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_TOPIC_HAS_NO_ANNOTATOR_RESUMES_FOR_AI);
        }
        if (topic.getProfileType() != AccountProfileTypeEnum.ANNOTATOR) {
            topic.setStatus(TopicStatusEnum.AI_ANALYSIS_PENDING);
            topicRepository.save(topic);
            topicHistoryRepository.save(
                    TopicHistory.builder()
                            .account(topic.getAccount())
                            .topic(topic)
                            .status(TopicStatusEnum.AI_ANALYSIS_PENDING)
                            .build()
            );
        }
    }

    @Transactional
    public void processQueuedConsolidation(Long accountId, Long topicId) {
        Account owner = getAccountOrThrowAndAssertActive(accountId);
        Topic topic = getTopicOwnedByAccountOrThrow(topicId, accountId);
        List<TopicAnnotatorLinkHistory> historyRows =
                topicAnnotatorLinkHistoryRepository.findAllWithNonBlankResumeByTopicIdOrderByCreatedAtAsc(topicId);
        if (historyRows.isEmpty()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_TOPIC_HAS_NO_ANNOTATOR_RESUMES_FOR_AI);
        }
        String userPayloadJson = buildUserPayloadJson(topic, historyRows);
        String acceptedModelContent = runConsolidationWithReviewLoop(topic, userPayloadJson);
        validateModelResponseJson(accountId, topicId, acceptedModelContent);

        TopicAiReport saved = topicAiReportRepository.save(
                TopicAiReport.builder()
                        .topic(topic)
                        .topicOwnerAccount(owner)
                        .openaiModel(openAiProperties.resolvePersistedModelLabel())
                        .requestPayloadJson(userPayloadJson)
                        .responsePayloadJson(acceptedModelContent)
                        .build()
        );

        if (topic.getProfileType() != AccountProfileTypeEnum.ANNOTATOR) {
            topic.setStatus(TopicStatusEnum.AI_REPORT_READY);
            topicRepository.save(topic);
            topicHistoryRepository.save(
                    TopicHistory.builder()
                            .account(owner)
                            .topic(topic)
                            .status(TopicStatusEnum.AI_REPORT_READY)
                            .build()
            );
        }

        log.info(
                TopicAiConsolidationLogConstants.CONSOLIDATION_FLOW_COMPLETED,
                accountId,
                topicId,
                saved.getId()
        );
    }

    private String runConsolidationWithReviewLoop(Topic topic, String userPayloadJson) {
        String lastProblematicText = TopicAiReviewConstants.DEFAULT_PROBLEMATIC_TEXT_FALLBACK;
        for (int attempt = 1; attempt <= TopicAiReviewConstants.MAX_REVIEW_ATTEMPTS; attempt++) {
            String modelContent = openAiChatCompletionPort.completeChat(
                    openAiProperties.getConsolidationSystemPrompt(),
                    userPayloadJson
            );
            OpenAiAssistantReviewResult reviewResult = openAiAssistantReviewPort.review(modelContent);
            if (reviewResult.accept()) {
                return modelContent;
            }
            if (reviewResult.problematicText() != null && !reviewResult.problematicText().isBlank()) {
                lastProblematicText = reviewResult.problematicText();
            }
        }
        topicAiProcessingErrorRepository.save(
                TopicAiProcessingError.builder()
                        .topic(topic)
                        .problematicText(lastProblematicText)
                        .build()
        );
        throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_REVIEW_REJECTED_MAX_ATTEMPTS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicAiReportResponse> listReportsByTopic(Long accountId, Long topicId) {
        getAccountOrThrowAndAssertActive(accountId);
        topicRepository.findByIdAndAccountId(topicId, accountId)
                .orElseThrow(() -> new AccountException(TopicValidationConstants.MESSAGE_TOPIC_NOT_FOUND_OR_NOT_OWNED));
        return topicAiReportRepository.findByTopicIdOrderByCreatedAtDesc(topicId).stream()
                .map(r -> TopicAiReportMapper.toResponse(r, objectMapper))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TopicAiReportPageResponse listReportsByOwnerAccount(Long accountId, Integer page, Integer size) {
        getAccountOrThrowAndAssertActive(accountId);
        int resolvedPage = page == null ? TopicPaginationConstants.DEFAULT_PAGE_NUMBER : page;
        int resolvedSize = size == null ? TopicPaginationConstants.DEFAULT_PAGE_SIZE : size;
        assertTopicAiReportPagination(resolvedPage, resolvedSize);

        Pageable pageable = PageRequest.of(
                resolvedPage,
                resolvedSize,
                Sort.by(Sort.Direction.DESC, TopicPaginationConstants.SORT_PROPERTY_CREATED_AT)
                        .and(Sort.by(Sort.Direction.DESC, TopicPaginationConstants.SORT_PROPERTY_ID))
        );
        Page<TopicAiReport> pageResult =
                topicAiReportRepository.findByTopicOwnerAccountIdOrderByCreatedAtDesc(accountId, pageable);
        List<TopicAiReportResponse> content = pageResult.stream()
                .map(r -> TopicAiReportMapper.toResponse(r, objectMapper))
                .toList();
        return new TopicAiReportPageResponse(
                content,
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getSize(),
                pageResult.getNumber(),
                pageResult.isFirst(),
                pageResult.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public JsonNode getLatestResponsePayload(Long accountId, Long topicId) {
        getAccountOrThrowAndAssertActive(accountId);
        Topic topic = getTopicOwnedByAccountOrThrow(topicId, accountId);
        if (topic.getStatus() != TopicStatusEnum.AI_REPORT_READY) {
            throw new AccountException(TopicValidationConstants.MESSAGE_TOPIC_STATUS_NOT_READY_FOR_AI_RESPONSE);
        }
        TopicAiReport latest = topicAiReportRepository.findLatestByTopicIdAndTopicOwnerAccountId(topicId, accountId)
                .orElseThrow(() -> new AccountException(TopicValidationConstants.MESSAGE_TOPIC_AI_REPORT_NOT_FOUND));
        try {
            return objectMapper.readTree(latest.getResponsePayloadJson());
        } catch (Exception ex) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON, ex);
        }
    }

    private static void assertTopicEligibleForAiConsolidation(Topic topic) {
        if (topic.getProfileType() == AccountProfileTypeEnum.ANNOTATOR) {
            return;
        }
        if (topic.getStatus() != TopicStatusEnum.OPEN) {
            throw new AccountException(TopicValidationConstants.MESSAGE_TOPIC_MUST_BE_OPEN_FOR_AI_CONSOLIDATION);
        }
    }

    private String buildUserPayloadJson(Topic topic, List<TopicAnnotatorLinkHistory> historyRows) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put(TopicAiUserPayloadJsonConstants.TOPIC_TITLE, topic.getTitle());
            if (topic.getContext() == null) {
                root.putNull(TopicAiUserPayloadJsonConstants.TOPIC_CONTEXT);
            } else {
                root.put(
                        TopicAiUserPayloadJsonConstants.TOPIC_CONTEXT,
                        TopicAiPayloadTextNormalizer.normalizeMultilineForAiPayload(topic.getContext())
                );
            }
            root.put(TopicAiUserPayloadJsonConstants.PROFILE_TYPE, topic.getProfileType().name());
            ArrayNode notes = root.putArray(TopicAiUserPayloadJsonConstants.ANNOTATOR_NOTES);
            for (TopicAnnotatorLinkHistory row : historyRows) {
                ObjectNode rowJson = notes.addObject();
                rowJson.put(TopicAiUserPayloadJsonConstants.HISTORY_ENTRY_ID, row.getId());
                rowJson.put(
                        TopicAiUserPayloadJsonConstants.ANNOTATOR_ACCOUNT_ID,
                        row.getAnnotatorAccount().getId()
                );
                rowJson.put(
                        TopicAiUserPayloadJsonConstants.RESUME,
                        TopicAiPayloadTextNormalizer.normalizeMultilineForAiPayload(row.getResume())
                );
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception ex) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON, ex);
        }
    }

    private void validateModelResponseJson(Long accountId, Long topicId, String rawJson) {
        try {
            JsonNode node = objectMapper.readTree(rawJson);
            if (!node.hasNonNull(TopicAiModelResponseJsonConstants.SUMMARY)
                    || !node.has(TopicAiModelResponseJsonConstants.RESPONSE_CONTEXT)
                    || !node.hasNonNull(TopicAiModelResponseJsonConstants.CONSOLIDATION_INSIGHT)
                    || !node.has(TopicAiModelResponseJsonConstants.SEGMENTS)
                    || !node.has(TopicAiModelResponseJsonConstants.BIBLE_REFERENCES)
                    || !node.has(TopicAiModelResponseJsonConstants.MODERATION)) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
            }
            assertResponseContextNodeValid(node.get(TopicAiModelResponseJsonConstants.RESPONSE_CONTEXT));
            validateSegmentsArray(node.get(TopicAiModelResponseJsonConstants.SEGMENTS));
            validateBibleReferencesArray(node.get(TopicAiModelResponseJsonConstants.BIBLE_REFERENCES));
            validateModerationObject(node.get(TopicAiModelResponseJsonConstants.MODERATION));
        } catch (AccountException ex) {
            logModelResponseValidationFailure(accountId, topicId, rawJson, ex);
            throw ex;
        } catch (Exception ex) {
            logModelResponseValidationFailure(accountId, topicId, rawJson, ex);
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON, ex);
        }
    }

    private String normalizeModelResponseJson(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode segmentsNode = root.get(TopicAiModelResponseJsonConstants.SEGMENTS);
            if (segmentsNode != null && segmentsNode.isArray()) {
                normalizeSegments((ArrayNode) segmentsNode);
            }
            removeModeratedItemsFromSegments(root);
            return objectMapper.writeValueAsString(root);
        } catch (Exception ex) {
            return rawJson;
        }
    }

    private void normalizeSegments(ArrayNode segments) {
        for (JsonNode segmentNode : segments) {
            if (!segmentNode.isObject()) {
                continue;
            }
            ObjectNode segment = (ObjectNode) segmentNode;
            JsonNode contributionsNode = segment.get(TopicAiModelResponseJsonConstants.CONTRIBUTIONS);
            if (contributionsNode == null || !contributionsNode.isArray() || contributionsNode.isEmpty()) {
                continue;
            }
            ArrayNode contributions = (ArrayNode) contributionsNode;
            JsonNode segmentTextNode = segment.get(TopicAiModelResponseJsonConstants.TEXT);
            boolean hasSegmentText = segmentTextNode != null && segmentTextNode.isTextual() && !segmentTextNode.asText().isBlank();
            if (!hasSegmentText) {
                JsonNode firstExcerpt = contributions.get(FIRST_ARRAY_INDEX).get(TopicAiModelResponseJsonConstants.EXCERPT);
                if (firstExcerpt != null && firstExcerpt.isTextual() && !firstExcerpt.asText().isBlank()) {
                    segment.put(TopicAiModelResponseJsonConstants.TEXT, firstExcerpt.asText());
                }
            }
            normalizeContributions(contributions);
        }
    }

    private void removeModeratedItemsFromSegments(JsonNode root) {
        JsonNode moderationNode = root.get(TopicAiModelResponseJsonConstants.MODERATION);
        JsonNode segmentsNode = root.get(TopicAiModelResponseJsonConstants.SEGMENTS);
        if (moderationNode == null
                || !moderationNode.isObject()
                || segmentsNode == null
                || !segmentsNode.isArray()
                || segmentsNode.isEmpty()) {
            return;
        }
        JsonNode removedItemsNode = moderationNode.get(TopicAiModelResponseJsonConstants.REMOVED_ITEMS);
        if (removedItemsNode == null || !removedItemsNode.isArray() || removedItemsNode.isEmpty()) {
            return;
        }
        ArrayNode segments = (ArrayNode) segmentsNode;
        for (int segmentIndex = segments.size() - 1; segmentIndex >= 0; segmentIndex--) {
            JsonNode segmentNode = segments.get(segmentIndex);
            if (!segmentNode.isObject()) {
                continue;
            }
            JsonNode contributionsNode = segmentNode.get(TopicAiModelResponseJsonConstants.CONTRIBUTIONS);
            if (contributionsNode == null || !contributionsNode.isArray()) {
                continue;
            }
            ArrayNode contributions = (ArrayNode) contributionsNode;
            for (int contributionIndex = contributions.size() - 1; contributionIndex >= 0; contributionIndex--) {
                JsonNode contributionNode = contributions.get(contributionIndex);
                if (shouldRemoveContributionByModeration(contributionNode, removedItemsNode)) {
                    contributions.remove(contributionIndex);
                }
            }
            if (contributions.isEmpty()) {
                segments.remove(segmentIndex);
            }
        }
    }

    private boolean shouldRemoveContributionByModeration(JsonNode contributionNode, JsonNode removedItemsNode) {
        if (contributionNode == null || !contributionNode.isObject()) {
            return false;
        }
        JsonNode excerptNode = contributionNode.get(TopicAiModelResponseJsonConstants.EXCERPT);
        JsonNode idsNode = contributionNode.get(TopicAiModelResponseJsonConstants.ANNOTATOR_ACCOUNT_IDS);
        if (excerptNode == null || !excerptNode.isTextual() || idsNode == null || !idsNode.isArray() || idsNode.size() != 1) {
            return false;
        }
        long singleAnnotatorId = idsNode.get(FIRST_ARRAY_INDEX).asLong(Long.MIN_VALUE);
        if (singleAnnotatorId == Long.MIN_VALUE) {
            return false;
        }
        String normalizedExcerpt = TopicAiPayloadTextNormalizer.normalizeForGrouping(excerptNode.asText());
        for (JsonNode removedItemNode : removedItemsNode) {
            if (!removedItemNode.isObject()
                    || !removedItemNode.has(TopicAiUserPayloadJsonConstants.ANNOTATOR_ACCOUNT_ID)
                    || !removedItemNode.get(TopicAiUserPayloadJsonConstants.ANNOTATOR_ACCOUNT_ID).isIntegralNumber()
                    || !removedItemNode.hasNonNull(TopicAiModelResponseJsonConstants.TEXT)) {
                continue;
            }
            long removedAnnotatorId = removedItemNode.get(TopicAiUserPayloadJsonConstants.ANNOTATOR_ACCOUNT_ID).asLong();
            if (removedAnnotatorId != singleAnnotatorId) {
                continue;
            }
            String normalizedRemovedText = TopicAiPayloadTextNormalizer.normalizeForGrouping(
                    removedItemNode.get(TopicAiModelResponseJsonConstants.TEXT).asText()
            );
            if (normalizedRemovedText.equals(normalizedExcerpt)) {
                return true;
            }
        }
        return false;
    }

    private void normalizeContributions(ArrayNode contributions) {
        for (JsonNode contributionNode : contributions) {
            if (!contributionNode.isObject()) {
                continue;
            }
            ObjectNode contribution = (ObjectNode) contributionNode;
            normalizeAnnotatorAccountIds(contribution);
            normalizeAnnotatorRawWhenRedundant(contribution);
        }
    }

    private void normalizeAnnotatorAccountIds(ObjectNode contribution) {
        JsonNode idsNode = contribution.get(TopicAiModelResponseJsonConstants.ANNOTATOR_ACCOUNT_IDS);
        if (idsNode == null || !idsNode.isArray() || idsNode.isEmpty()) {
            return;
        }
        Set<Long> uniqueOrderedIds = new TreeSet<>();
        for (JsonNode idNode : idsNode) {
            if (idNode != null && idNode.isIntegralNumber()) {
                uniqueOrderedIds.add(idNode.asLong());
            }
        }
        ArrayNode normalizedIds = objectMapper.createArrayNode();
        uniqueOrderedIds.forEach(normalizedIds::add);
        contribution.set(TopicAiModelResponseJsonConstants.ANNOTATOR_ACCOUNT_IDS, normalizedIds);
    }

    private void normalizeAnnotatorRawWhenRedundant(ObjectNode contribution) {
        JsonNode excerptNode = contribution.get(TopicAiModelResponseJsonConstants.EXCERPT);
        JsonNode rawNode = contribution.get(TopicAiModelResponseJsonConstants.ANNOTATOR_RAW);
        if (excerptNode == null || !excerptNode.isTextual() || rawNode == null || !rawNode.isArray() || rawNode.isEmpty()) {
            return;
        }
        String normalizedExcerpt = TopicAiPayloadTextNormalizer.normalizeForGrouping(excerptNode.asText());
        boolean allEquivalent = true;
        for (JsonNode rawItem : rawNode) {
            if (!rawItem.isObject() || !rawItem.hasNonNull(TopicAiModelResponseJsonConstants.TEXT)) {
                allEquivalent = false;
                break;
            }
            String normalizedRawText =
                    TopicAiPayloadTextNormalizer.normalizeForGrouping(rawItem.get(TopicAiModelResponseJsonConstants.TEXT).asText());
            if (!normalizedExcerpt.equals(normalizedRawText)) {
                allEquivalent = false;
                break;
            }
        }
        if (allEquivalent) {
            contribution.remove(TopicAiModelResponseJsonConstants.ANNOTATOR_RAW);
        }
    }

    private void logModelResponseValidationFailure(Long accountId, Long topicId, String rawJson, Throwable cause) {
        int length = rawJson == null ? 0 : rawJson.length();
        String preview = buildModelResponseLogPreview(rawJson);
        String causeMessage = cause == null ? "" : String.valueOf(cause.getMessage());
        log.warn(
                TopicAiConsolidationLogConstants.MODEL_RESPONSE_INVALID_JSON,
                accountId,
                topicId,
                length,
                preview,
                causeMessage
        );
    }

    private static String buildModelResponseLogPreview(String rawJson) {
        if (rawJson == null) {
            return TopicAiConsolidationLogConstants.MODEL_RESPONSE_PREVIEW_NULL_PLACEHOLDER;
        }
        int max = TopicAiConsolidationLogConstants.MODEL_RESPONSE_LOG_PREVIEW_MAX_CHARS;
        if (rawJson.length() <= max) {
            return rawJson;
        }
        return rawJson.substring(0, max) + TopicAiConsolidationLogConstants.MODEL_RESPONSE_PREVIEW_TRUNCATED_SUFFIX;
    }

    private static void assertResponseContextNodeValid(JsonNode contextNode) {
        if (contextNode.isNull()) {
            return;
        }
        if (!contextNode.isTextual()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
        }
    }

    private static void validateSegmentsArray(JsonNode segments) {
        if (!segments.isArray()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
        }
        for (JsonNode segment : segments) {
            if (!segment.hasNonNull(TopicAiModelResponseJsonConstants.TEXT)
                    || !segment.has(TopicAiModelResponseJsonConstants.CONTRIBUTIONS)
                    || !segment.get(TopicAiModelResponseJsonConstants.CONTRIBUTIONS).isArray()
                    || segment.get(TopicAiModelResponseJsonConstants.CONTRIBUTIONS).isEmpty()) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
            }
            for (JsonNode contribution : segment.get(TopicAiModelResponseJsonConstants.CONTRIBUTIONS)) {
                validateContribution(contribution);
            }
        }
    }

    private static void validateContribution(JsonNode contribution) {
        if (!contribution.has(TopicAiModelResponseJsonConstants.EXCERPT)) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
        }
        JsonNode excerptNode = contribution.get(TopicAiModelResponseJsonConstants.EXCERPT);
        if (excerptNode.isTextual()) {
            if (excerptNode.asText().isBlank()) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
            }
            boolean hasIds = contribution.has(TopicAiModelResponseJsonConstants.ANNOTATOR_ACCOUNT_IDS);
            boolean hasRaw = contribution.has(TopicAiModelResponseJsonConstants.ANNOTATOR_RAW);
            if (hasIds) {
                validateAnnotatorAccountIdsArray(
                        contribution.get(TopicAiModelResponseJsonConstants.ANNOTATOR_ACCOUNT_IDS)
                );
                if (hasRaw) {
                    validateAnnotatorRawArray(contribution.get(TopicAiModelResponseJsonConstants.ANNOTATOR_RAW));
                }
                return;
            }
            if (hasRaw) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
            }
            return;
        }
        if (excerptNode.isArray()) {
            validateExcerptAsGroupedClusters(excerptNode);
            return;
        }
        throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
    }

    private static void validateAnnotatorAccountIdsArray(JsonNode idsNode) {
        if (!idsNode.isArray() || idsNode.isEmpty()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
        }
        for (JsonNode idNode : idsNode) {
            if (!idNode.isIntegralNumber()) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
            }
        }
    }

    private static void validateAnnotatorRawArray(JsonNode rawArray) {
        if (!rawArray.isArray() || rawArray.isEmpty()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
        }
        for (JsonNode raw : rawArray) {
            if (!raw.has(TopicAiUserPayloadJsonConstants.ANNOTATOR_ACCOUNT_ID)
                    || !raw.get(TopicAiUserPayloadJsonConstants.ANNOTATOR_ACCOUNT_ID).isIntegralNumber()
                    || !raw.hasNonNull(TopicAiModelResponseJsonConstants.TEXT)) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
            }
        }
    }

    /**
     * Formato do agente: {@code excerpt} como array de grupos com {@code annotator_raw} e {@code groupTitle} opcional.
     */
    private static void validateExcerptAsGroupedClusters(JsonNode excerptGroups) {
        if (excerptGroups.isEmpty()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
        }
        for (JsonNode group : excerptGroups) {
            if (!group.isObject()) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
            }
            if (group.has(TopicAiModelResponseJsonConstants.GROUP_TITLE)
                    && !group.get(TopicAiModelResponseJsonConstants.GROUP_TITLE).isTextual()) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
            }
            if (!group.has(TopicAiModelResponseJsonConstants.ANNOTATOR_RAW)) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
            }
            validateAnnotatorRawArray(group.get(TopicAiModelResponseJsonConstants.ANNOTATOR_RAW));
        }
    }

    private static void validateBibleReferencesArray(JsonNode bibleRefs) {
        if (!bibleRefs.isArray()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
        }
        for (JsonNode ref : bibleRefs) {
            if (!ref.has(TopicAiModelResponseJsonConstants.REFERENCE_TEXT)) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
            }
            JsonNode refText = ref.get(TopicAiModelResponseJsonConstants.REFERENCE_TEXT);
            if (refText.isTextual()) {
                if (refText.asText().isBlank()) {
                    throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
                }
            } else if (refText.isArray()) {
                if (refText.isEmpty()) {
                    throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
                }
                for (JsonNode line : refText) {
                    if (!line.isTextual() || line.asText().isBlank()) {
                        throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
                    }
                }
            } else {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
            }
            if (ref.has(TopicAiModelResponseJsonConstants.VERSE_LIMIT)
                    && !ref.get(TopicAiModelResponseJsonConstants.VERSE_LIMIT).isIntegralNumber()) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
            }
        }
    }

    private static void validateModerationObject(JsonNode moderation) {
        if (!moderation.has(TopicAiModelResponseJsonConstants.REMOVED_ITEMS)
                || !moderation.get(TopicAiModelResponseJsonConstants.REMOVED_ITEMS).isArray()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
        }
        for (JsonNode removed : moderation.get(TopicAiModelResponseJsonConstants.REMOVED_ITEMS)) {
            if (!removed.has(TopicAiUserPayloadJsonConstants.ANNOTATOR_ACCOUNT_ID)
                    || !removed.get(TopicAiUserPayloadJsonConstants.ANNOTATOR_ACCOUNT_ID).isIntegralNumber()
                    || !removed.hasNonNull(TopicAiModelResponseJsonConstants.TEXT)) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
            }
        }
        if (moderation.has(TopicAiModelResponseJsonConstants.NOTES)) {
            JsonNode notesNode = moderation.get(TopicAiModelResponseJsonConstants.NOTES);
            if (!notesNode.isNull() && !notesNode.isTextual()) {
                throw new AccountException(TopicValidationConstants.MESSAGE_OPENAI_RESPONSE_NOT_VALID_JSON);
            }
        }
    }

    private Account getAccountOrThrowAndAssertActive(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountException(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
        if (account.getStatus() != AccountStatusEnum.ACTIVE) {
            log.warn(TopicServiceLogConstants.TOPIC_ENDPOINT_REJECTED_ACCOUNT_NOT_ACTIVE);
            throw new AccountException(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS);
        }
        return account;
    }

    private Topic getTopicOwnedByAccountOrThrow(Long topicId, Long accountId) {
        return topicRepository.findByIdAndAccountId(topicId, accountId)
                .orElseThrow(() -> new AccountException(TopicValidationConstants.MESSAGE_TOPIC_NOT_FOUND_OR_NOT_OWNED));
    }

    private static void assertTopicAiReportPagination(int pageNumber, int pageSize) {
        if (pageNumber < TopicPaginationConstants.MIN_PAGE_NUMBER) {
            throw new AccountException(TopicValidationConstants.MESSAGE_TOPIC_PAGE_NUMBER_NEGATIVE);
        }
        if (pageSize < TopicPaginationConstants.MIN_PAGE_SIZE || pageSize > TopicPaginationConstants.MAX_PAGE_SIZE) {
            throw new AccountException(
                    TopicValidationConstants.MESSAGE_TOPIC_PAGE_SIZE_INVALID.formatted(
                            TopicPaginationConstants.MIN_PAGE_SIZE,
                            TopicPaginationConstants.MAX_PAGE_SIZE
                    )
            );
        }
    }
}
