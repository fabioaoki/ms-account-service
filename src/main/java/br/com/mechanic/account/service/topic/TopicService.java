package br.com.mechanic.account.service.topic;

import br.com.mechanic.account.constant.AccountUpdateValidationConstants;
import br.com.mechanic.account.constant.TopicCreationConstants;
import br.com.mechanic.account.constant.TopicServiceLogConstants;
import br.com.mechanic.account.constant.EntityConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.topic.Topic;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import br.com.mechanic.account.exception.AccountException;
import br.com.mechanic.account.mapper.topic.TopicMapper;
import br.com.mechanic.account.repository.account.impl.AccountProfileRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.AccountRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.TopicRepositoryImpl;
import br.com.mechanic.account.service.request.TopicCreateRequest;
import br.com.mechanic.account.service.request.TopicUpdateRequest;
import br.com.mechanic.account.service.response.TopicResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Topic creation: account must exist, be {@link AccountStatusEnum#ACTIVE}; missing {@code profile_type} implies
 * {@link AccountProfileTypeEnum#ANNOTATOR}. Other profiles require a row in account_profile.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TopicService implements TopicServiceBO {

    private final AccountRepositoryImpl accountRepository;
    private final AccountProfileRepositoryImpl accountProfileRepository;
    private final TopicRepositoryImpl topicRepository;
    private final Clock clock;

    @Override
    @Transactional
    public TopicResponse create(Long accountId, TopicCreateRequest request) {
        log.info(TopicServiceLogConstants.CREATE_TOPIC_FLOW_STARTED, accountId);
        Account account = getAccountOrThrow(accountId);
        assertAccountActiveForTopicCreation(account);
        if (request.profileType() == null && request.endDate() != null) {
            throw new AccountException(TopicValidationConstants.MESSAGE_PROFILE_TYPE_REQUIRED_WHEN_END_DATE_PRESENT);
        }
        AccountProfileTypeEnum effectiveProfileType = request.profileType() != null
                ? request.profileType()
                : AccountProfileTypeEnum.ANNOTATOR;
        assertProfileTypeLinkedToAccountForCreate(accountId, effectiveProfileType);
        LocalDateTime creationTimestamp = LocalDateTime.now(clock);
        assertEndDateRulesForProfileCreate(effectiveProfileType, request.endDate(), creationTimestamp);
        String normalizedTitle = validateAndNormalizeTitle(request.title());
        String normalizedContext = normalizeContext(request.context());
        Topic topic = TopicMapper.toEntityForPersist(
                account,
                normalizedTitle,
                normalizedContext,
                effectiveProfileType,
                creationTimestamp,
                request.endDate()
        );
        Topic saved = topicRepository.save(topic);
        log.info(TopicServiceLogConstants.CREATE_TOPIC_FLOW_COMPLETED, accountId, saved.getId());
        return TopicMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TopicResponse update(Long accountId, Long topicId, TopicUpdateRequest request) {
        log.info(TopicServiceLogConstants.UPDATE_TOPIC_FLOW_STARTED, accountId, topicId);
        Account account = getAccountOrThrow(accountId);
        assertAccountActiveForTopicUpdate(account);
        Topic topic = topicRepository.findByIdAndAccountId(topicId, accountId)
                .orElseThrow(() -> new AccountException(TopicValidationConstants.MESSAGE_TOPIC_NOT_FOUND_OR_NOT_OWNED));

        if (topic.getProfileType() == AccountProfileTypeEnum.ANNOTATOR) {
            assertAnnotatorTopicUpdateForbiddenFields(request);
            if (!request.isTitlePresent()) {
                throw new AccountException(TopicValidationConstants.MESSAGE_ANNOTATOR_TOPIC_UPDATE_TITLE_REQUIRED);
            }
            topic.setTitle(validateAndNormalizeTitle(request.getTitle()));
        } else {
            assertNonAnnotatorTopicUpdateHasAtLeastOneField(request);
            if (request.isProfileTypePresent() && request.getProfileType() == AccountProfileTypeEnum.ANNOTATOR) {
                throw new AccountException(TopicValidationConstants.MESSAGE_NON_ANNOTATOR_TOPIC_CANNOT_CHANGE_TO_ANNOTATOR);
            }
            if (request.isProfileTypePresent()) {
                assertProfileTypeLinkedToAccountForUpdate(accountId, request.getProfileType());
            }
            if (request.isEndDatePresent()) {
                assertEndDateRulesForNonAnnotatorUpdate(request.getEndDate(), topic.getCreatedAt());
            }
            if (request.isTitlePresent()) {
                topic.setTitle(validateAndNormalizeTitle(request.getTitle()));
            }
            if (request.isContextPresent()) {
                topic.setContext(normalizeContext(request.getContext()));
            }
            if (request.isProfileTypePresent()) {
                topic.setProfileType(request.getProfileType());
            }
            if (request.isEndDatePresent()) {
                topic.setEndDate(request.getEndDate());
            }
        }

        Topic saved = topicRepository.save(topic);
        log.info(TopicServiceLogConstants.UPDATE_TOPIC_FLOW_COMPLETED, accountId, saved.getId());
        return TopicMapper.toResponse(saved);
    }

    private void assertAccountActiveForTopicCreation(Account account) {
        if (account.getStatus() != AccountStatusEnum.ACTIVE) {
            log.warn(TopicServiceLogConstants.CREATE_TOPIC_REJECTED_ACCOUNT_NOT_ACTIVE);
            throw new AccountException(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_TO_CREATE_TOPIC);
        }
    }

    private void assertAccountActiveForTopicUpdate(Account account) {
        if (account.getStatus() != AccountStatusEnum.ACTIVE) {
            log.warn(TopicServiceLogConstants.UPDATE_TOPIC_REJECTED_ACCOUNT_NOT_ACTIVE);
            throw new AccountException(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_TO_UPDATE_TOPIC);
        }
    }

    private void assertProfileTypeLinkedToAccountForCreate(Long accountId, AccountProfileTypeEnum profileType) {
        if (accountProfileRepository.findByAccountIdAndProfileType(accountId, profileType).isEmpty()) {
            log.warn(TopicServiceLogConstants.CREATE_TOPIC_REJECTED_PROFILE_NOT_LINKED);
            throw new AccountException(TopicValidationConstants.MESSAGE_PROFILE_TYPE_NOT_LINKED_TO_ACCOUNT);
        }
    }

    private void assertProfileTypeLinkedToAccountForUpdate(Long accountId, AccountProfileTypeEnum profileType) {
        if (accountProfileRepository.findByAccountIdAndProfileType(accountId, profileType).isEmpty()) {
            log.warn(TopicServiceLogConstants.UPDATE_TOPIC_REJECTED_PROFILE_NOT_LINKED);
            throw new AccountException(TopicValidationConstants.MESSAGE_PROFILE_TYPE_NOT_LINKED_TO_ACCOUNT);
        }
    }

    private static void assertAnnotatorTopicUpdateForbiddenFields(TopicUpdateRequest request) {
        if (request.isProfileTypePresent() || request.isEndDatePresent() || request.isContextPresent()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_ANNOTATOR_TOPIC_UPDATE_FORBIDDEN_FIELDS);
        }
    }

    private static void assertNonAnnotatorTopicUpdateHasAtLeastOneField(TopicUpdateRequest request) {
        if (!request.isTitlePresent()
                && !request.isContextPresent()
                && !request.isProfileTypePresent()
                && !request.isEndDatePresent()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_TOPIC_UPDATE_AT_LEAST_ONE_FIELD);
        }
    }

    /**
     * {@code end_date} relativo ao registro original do tópico ({@code topic.createdAt}), não ao relógio atual.
     */
    private void assertEndDateRulesForNonAnnotatorUpdate(LocalDateTime endDate, LocalDateTime topicCreatedAt) {
        LocalDateTime maxEnd = topicCreatedAt.plusDays(TopicCreationConstants.NON_ANNOTATOR_END_DATE_MAX_OFFSET_DAYS);
        if (endDate.isBefore(topicCreatedAt)) {
            throw new AccountException(TopicValidationConstants.MESSAGE_END_DATE_MUST_NOT_BE_BEFORE_CREATION);
        }
        if (endDate.isAfter(maxEnd)) {
            throw new AccountException(
                    TopicValidationConstants.MESSAGE_END_DATE_MUST_NOT_EXCEED_CREATION_PLUS_MAX_DAYS.formatted(
                            TopicCreationConstants.NON_ANNOTATOR_END_DATE_MAX_OFFSET_DAYS
                    )
            );
        }
    }

    /**
     * ANNOTATOR: no {@code end_date}. Other profiles: required and within
     * {@code [creationTimestamp, creationTimestamp + NON_ANNOTATOR_END_DATE_MAX_OFFSET_DAYS]} (inclusive).
     */
    private void assertEndDateRulesForProfileCreate(
            AccountProfileTypeEnum profileType,
            LocalDateTime endDate,
            LocalDateTime creationTimestamp
    ) {
        if (profileType == AccountProfileTypeEnum.ANNOTATOR) {
            if (endDate != null) {
                throw new AccountException(TopicValidationConstants.MESSAGE_ANNOTATOR_TOPIC_CANNOT_SEND_END_DATE);
            }
            return;
        }
        if (endDate == null) {
            throw new AccountException(TopicValidationConstants.MESSAGE_END_DATE_REQUIRED_FOR_NON_ANNOTATOR_TOPIC);
        }
        LocalDateTime maxEnd = creationTimestamp.plusDays(TopicCreationConstants.NON_ANNOTATOR_END_DATE_MAX_OFFSET_DAYS);
        if (endDate.isBefore(creationTimestamp)) {
            throw new AccountException(TopicValidationConstants.MESSAGE_END_DATE_MUST_NOT_BE_BEFORE_CREATION);
        }
        if (endDate.isAfter(maxEnd)) {
            throw new AccountException(
                    TopicValidationConstants.MESSAGE_END_DATE_MUST_NOT_EXCEED_CREATION_PLUS_MAX_DAYS.formatted(
                            TopicCreationConstants.NON_ANNOTATOR_END_DATE_MAX_OFFSET_DAYS
                    )
            );
        }
    }

    private Account getAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountException(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
    }

    private static String validateAndNormalizeTitle(String rawTitle) {
        if (rawTitle == null) {
            throw new AccountException(TopicValidationConstants.MESSAGE_TOPIC_TITLE_REQUIRED);
        }
        String trimmed = rawTitle.trim();
        if (trimmed.isEmpty()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_TOPIC_TITLE_REQUIRED);
        }
        if (trimmed.length() < TopicValidationConstants.MIN_TOPIC_TITLE_CHAR_COUNT_AFTER_TRIM) {
            throw new AccountException(TopicValidationConstants.MESSAGE_TOPIC_TITLE_INVALID_LENGTH);
        }
        if (trimmed.length() > EntityConstants.TOPIC_TITLE_COLUMN_LENGTH) {
            throw new AccountException(
                    TopicValidationConstants.MESSAGE_TOPIC_TITLE_EXCEEDS_MAX_LENGTH.formatted(
                            EntityConstants.TOPIC_TITLE_COLUMN_LENGTH
                    )
            );
        }
        return trimmed;
    }

    private static String normalizeContext(String rawContext) {
        if (rawContext == null) {
            return null;
        }
        String trimmed = rawContext.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }
}
