package br.com.mechanic.account.service.annotator;

import br.com.mechanic.account.constant.AccountUpdateValidationConstants;
import br.com.mechanic.account.constant.AnnotatorLinkServiceLogConstants;
import br.com.mechanic.account.constant.TopicAnnotatorLinkValidationConstants;
import br.com.mechanic.account.constant.TopicPaginationConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.topic.Topic;
import br.com.mechanic.account.entity.topic.TopicAnnotatorLink;
import br.com.mechanic.account.entity.topic.TopicAnnotatorLinkHistory;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import br.com.mechanic.account.exception.AccountException;
import br.com.mechanic.account.mapper.topic.AnnotatorLinkMapper;
import br.com.mechanic.account.repository.account.impl.AccountProfileRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.AccountRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.TopicAnnotatorLinkHistoryRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.TopicAnnotatorLinkRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.TopicRepositoryImpl;
import br.com.mechanic.account.service.request.TopicAnnotatorLinkCreateRequest;
import br.com.mechanic.account.service.request.TopicAnnotatorLinkResumeUpdateRequest;
import br.com.mechanic.account.service.response.TopicAnnotatorLinkAnnotatorListItemResponse;
import br.com.mechanic.account.service.response.TopicAnnotatorLinkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnnotatorLinkService implements AnnotatorLinkServiceBO {

    private final AccountRepositoryImpl accountRepository;
    private final AccountProfileRepositoryImpl accountProfileRepository;
    private final TopicRepositoryImpl topicRepository;
    private final TopicAnnotatorLinkRepositoryImpl topicAnnotatorLinkRepository;
    private final TopicAnnotatorLinkHistoryRepositoryImpl topicAnnotatorLinkHistoryRepository;
    private final Clock clock;

    @Override
    @Transactional
    public TopicAnnotatorLinkResponse createLink(
            Long topicOwnerAccountId,
            Long topicId,
            TopicAnnotatorLinkCreateRequest request
    ) {
        log.info(
                AnnotatorLinkServiceLogConstants.CREATE_LINK_FLOW_STARTED,
                topicOwnerAccountId,
                topicId,
                request.annotatorAccountId()
        );

        Account topicOwnerAccount = getAccountOrThrowAndAssertActiveForAnnotatorLink(topicOwnerAccountId);
        Topic topic = topicRepository.findByIdAndAccountId(topicId, topicOwnerAccountId)
                .orElseThrow(() -> new AccountException(
                        TopicAnnotatorLinkValidationConstants.MESSAGE_TOPIC_NOT_FOUND_OR_NOT_OWNED_FOR_LINK
                ));

        assertTopicOpenForNonAnnotatorProfileOrSkipAnnotatorWorkflow(topic);

        Account annotatorAccount = getAccountOrThrowAndAssertActiveForAnnotatorLink(request.annotatorAccountId());

        if (!accountProfileRepository.existsByAccountIdAndProfileType(
                annotatorAccount.getId(),
                AccountProfileTypeEnum.ANNOTATOR
        )) {
            throw new AccountException(TopicAnnotatorLinkValidationConstants.MESSAGE_ANNOTATOR_MUST_HAVE_ANNOTATOR_PROFILE);
        }

        if (topic.getProfileType() != AccountProfileTypeEnum.ANNOTATOR
                && topicOwnerAccount.getId().equals(annotatorAccount.getId())) {
            throw new AccountException(
                    TopicAnnotatorLinkValidationConstants.MESSAGE_ANNOTATOR_MUST_DIFFER_FROM_TOPIC_OWNER_UNLESS_TOPIC_PROFILE_IS_ANNOTATOR
            );
        }

        if (topicAnnotatorLinkRepository.existsByTopicIdAndAnnotatorAccountId(topic.getId(), annotatorAccount.getId())) {
            throw new AccountException(TopicAnnotatorLinkValidationConstants.MESSAGE_TOPIC_ANNOTATOR_LINK_PAIR_ALREADY_EXISTS);
        }

        LocalDateTime creationTimestamp = LocalDateTime.now(clock);

        TopicAnnotatorLink link = AnnotatorLinkMapper.toEntityForPersist(
                topic,
                topicOwnerAccount,
                annotatorAccount,
                null,
                creationTimestamp
        );

        TopicAnnotatorLink saved = topicAnnotatorLinkRepository.save(link);

        TopicAnnotatorLinkHistory history = AnnotatorLinkMapper.toInitialHistoryEntity(
                saved,
                annotatorAccount,
                null,
                creationTimestamp
        );
        topicAnnotatorLinkHistoryRepository.save(history);

        log.info(AnnotatorLinkServiceLogConstants.CREATE_LINK_FLOW_COMPLETED, saved.getId(), topicId);

        return AnnotatorLinkMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TopicAnnotatorLinkResponse updateLinkResume(
            Long topicOwnerAccountId,
            Long topicId,
            TopicAnnotatorLinkResumeUpdateRequest request
    ) {
        log.info(
                AnnotatorLinkServiceLogConstants.UPDATE_LINK_RESUME_FLOW_STARTED,
                topicOwnerAccountId,
                topicId,
                request.annotatorAccountId()
        );

        Account topicOwnerAccount = getAccountOrThrowAndAssertActiveForAnnotatorLink(topicOwnerAccountId);
        Topic topic = topicRepository.findByIdAndAccountId(topicId, topicOwnerAccountId)
                .orElseThrow(() -> new AccountException(
                        TopicAnnotatorLinkValidationConstants.MESSAGE_TOPIC_NOT_FOUND_OR_NOT_OWNED_FOR_LINK
                ));

        assertTopicOpenForResumeUpdate(topic);

        Account annotatorAccount = getAccountOrThrowAndAssertActiveForAnnotatorLink(request.annotatorAccountId());

        if (!accountProfileRepository.existsByAccountIdAndProfileType(
                annotatorAccount.getId(),
                AccountProfileTypeEnum.ANNOTATOR
        )) {
            throw new AccountException(TopicAnnotatorLinkValidationConstants.MESSAGE_ANNOTATOR_MUST_HAVE_ANNOTATOR_PROFILE);
        }

        TopicAnnotatorLink link = topicAnnotatorLinkRepository
                .findByTopicIdAndAnnotatorAccountId(topic.getId(), annotatorAccount.getId())
                .orElseThrow(() -> new AccountException(
                        TopicAnnotatorLinkValidationConstants.MESSAGE_TOPIC_ANNOTATOR_LINK_NOT_FOUND_FOR_RESUME
                ));

        if (!link.getTopicOwnerAccount().getId().equals(topicOwnerAccount.getId())) {
            throw new AccountException(
                    TopicAnnotatorLinkValidationConstants.MESSAGE_TOPIC_ANNOTATOR_LINK_NOT_FOUND_FOR_RESUME
            );
        }

        String normalizedResume = normalizeResumeRequired(request.resume());
        LocalDateTime historyTimestamp = LocalDateTime.now(clock);

        AnnotatorLinkMapper.applyResumeToLink(link, normalizedResume);
        TopicAnnotatorLink saved = topicAnnotatorLinkRepository.save(link);

        TopicAnnotatorLinkHistory history = AnnotatorLinkMapper.toInitialHistoryEntity(
                saved,
                annotatorAccount,
                normalizedResume,
                historyTimestamp
        );
        topicAnnotatorLinkHistoryRepository.save(history);

        log.info(AnnotatorLinkServiceLogConstants.UPDATE_LINK_RESUME_FLOW_COMPLETED, saved.getId(), topicId);

        return AnnotatorLinkMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicAnnotatorLinkAnnotatorListItemResponse> listLinksForAnnotatorView(
            Long annotatorAccountId,
            Long topicId,
            Long topicOwnerAccountId,
            TopicStatusEnum topicStatus
    ) {
        log.info(
                AnnotatorLinkServiceLogConstants.LIST_LINKS_FOR_ANNOTATOR_FLOW_STARTED,
                annotatorAccountId,
                topicId,
                topicOwnerAccountId,
                topicStatus
        );
        getAccountOrThrowAndAssertActiveForAnnotatorLink(annotatorAccountId);
        Sort sort = Sort.by(Sort.Direction.DESC, TopicPaginationConstants.SORT_PROPERTY_CREATED_AT);
        List<TopicAnnotatorLink> links = topicAnnotatorLinkRepository.findAllByAnnotatorAccountIdWithOptionalFilters(
                annotatorAccountId,
                topicId,
                topicOwnerAccountId,
                topicStatus,
                sort
        );
        return links.stream().map(AnnotatorLinkMapper::toAnnotatorListItem).toList();
    }

    /**
     * Criar vínculo: tópicos ANNOTATOR não usam status de workflow ({@code null}); demais exigem {@link TopicStatusEnum#OPEN}.
     */
    private static void assertTopicOpenForNonAnnotatorProfileOrSkipAnnotatorWorkflow(Topic topic) {
        if (topic.getProfileType() == AccountProfileTypeEnum.ANNOTATOR) {
            return;
        }
        if (topic.getStatus() != TopicStatusEnum.OPEN) {
            throw new AccountException(TopicAnnotatorLinkValidationConstants.MESSAGE_TOPIC_MUST_BE_OPEN_FOR_ANNOTATOR_LINK);
        }
    }

    /**
     * Atualizar resumo: para tópicos com workflow, apenas {@link TopicStatusEnum#OPEN}; tópicos ANNOTATOR sem status não são bloqueados.
     */
    private static void assertTopicOpenForResumeUpdate(Topic topic) {
        if (topic.getProfileType() == AccountProfileTypeEnum.ANNOTATOR) {
            return;
        }
        if (topic.getStatus() != TopicStatusEnum.OPEN) {
            throw new AccountException(TopicAnnotatorLinkValidationConstants.MESSAGE_TOPIC_MUST_BE_OPEN_FOR_ANNOTATOR_LINK_RESUME);
        }
    }

    private Account getAccountOrThrowAndAssertActiveForAnnotatorLink(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountException(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
        if (account.getStatus() != AccountStatusEnum.ACTIVE) {
            throw new AccountException(TopicAnnotatorLinkValidationConstants.MESSAGE_ACCOUNTS_MUST_BE_ACTIVE_FOR_ANNOTATOR_LINK);
        }
        return account;
    }

    private static String normalizeResumeRequired(String rawResume) {
        if (rawResume == null) {
            throw new AccountException(TopicAnnotatorLinkValidationConstants.MESSAGE_RESUME_REQUIRED);
        }
        String trimmed = rawResume.trim();
        if (trimmed.isEmpty()) {
            throw new AccountException(TopicAnnotatorLinkValidationConstants.MESSAGE_RESUME_REQUIRED);
        }
        if (trimmed.length() > TopicAnnotatorLinkValidationConstants.MAX_RESUME_CHAR_COUNT) {
            throw new AccountException(
                    TopicAnnotatorLinkValidationConstants.MESSAGE_RESUME_EXCEEDS_MAX_LENGTH.formatted(
                            TopicAnnotatorLinkValidationConstants.MAX_RESUME_CHAR_COUNT
                    )
            );
        }
        return trimmed;
    }
}
