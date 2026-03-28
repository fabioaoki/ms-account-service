package br.com.mechanic.account.service.topic;

import br.com.mechanic.account.constant.AccountUpdateValidationConstants;
import br.com.mechanic.account.constant.TopicServiceLogConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.topic.Topic;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import br.com.mechanic.account.exception.AccountException;
import br.com.mechanic.account.mapper.topic.TopicMapper;
import br.com.mechanic.account.repository.account.impl.AccountProfileRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.AccountRepositoryImpl;
import br.com.mechanic.account.repository.account.impl.TopicRepositoryImpl;
import br.com.mechanic.account.service.request.TopicCreateRequest;
import br.com.mechanic.account.service.response.TopicResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Criacao de topicos: o {@code profile_type} do pedido deve existir como vinculo na tabela
 * {@link br.com.mechanic.account.constant.EntityConstants#ACCOUNT_PROFILE_TABLE_NAME} para o
 * {@code accountId} (mesmo criterio do relacionamento conta-perfil no cadastro / link de perfis).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TopicService implements TopicServiceBO {

    private final AccountRepositoryImpl accountRepository;
    private final AccountProfileRepositoryImpl accountProfileRepository;
    private final TopicRepositoryImpl topicRepository;

    @Override
    @Transactional
    public TopicResponse create(Long accountId, TopicCreateRequest request) {
        log.info(TopicServiceLogConstants.CREATE_TOPIC_FLOW_STARTED, accountId);
        Account account = getAccountOrThrow(accountId);
        assertAccountActiveForTopicCreation(account);
        assertProfileTypeAllowedForTopicCreation(request.profileType());
        assertAccountProfileRowExistsForRequestedProfileType(accountId, request.profileType());
        String temaNormalizado = validateAndNormalizeTema(request.tema());
        String contextoNormalizado = normalizeContexto(request.contexto());
        Topic topic = Topic.builder()
                .account(account)
                .tema(temaNormalizado)
                .context(contextoNormalizado)
                .status(TopicStatusEnum.OPEN)
                .profileType(request.profileType())
                .build();
        Topic saved = topicRepository.save(topic);
        log.info(TopicServiceLogConstants.CREATE_TOPIC_FLOW_COMPLETED, accountId, saved.getId());
        return TopicMapper.toResponse(saved);
    }

    private void assertAccountActiveForTopicCreation(Account account) {
        if (account.getStatus() != AccountStatusEnum.ACTIVE) {
            log.warn(TopicServiceLogConstants.CREATE_TOPIC_REJECTED_ACCOUNT_NOT_ACTIVE);
            throw new AccountException(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_TO_CREATE_TOPIC);
        }
    }

    private void assertProfileTypeAllowedForTopicCreation(AccountProfileTypeEnum profileType) {
        if (profileType == AccountProfileTypeEnum.ANNOTATOR) {
            log.warn(TopicServiceLogConstants.CREATE_TOPIC_REJECTED_ANNOTATOR_PROFILE);
            throw new AccountException(TopicValidationConstants.MESSAGE_ANNOTATOR_CANNOT_CREATE_TOPIC);
        }
    }

    /**
     * Consulta {@link br.com.mechanic.account.constant.EntityConstants#ACCOUNT_PROFILE_TABLE_NAME}:
     * so permite criar topico se existir linha para esta conta cujo perfil (via
     * {@code profile.profile_type}) coincide com o tipo enviado.
     */
    private void assertAccountProfileRowExistsForRequestedProfileType(
            Long accountId,
            AccountProfileTypeEnum requestedProfileType
    ) {
        if (accountProfileRepository.findByAccountIdAndProfileType(accountId, requestedProfileType).isEmpty()) {
            log.warn(TopicServiceLogConstants.CREATE_TOPIC_REJECTED_PROFILE_NOT_LINKED);
            throw new AccountException(TopicValidationConstants.MESSAGE_PROFILE_TYPE_NOT_LINKED_TO_ACCOUNT);
        }
    }

    private Account getAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountException(AccountUpdateValidationConstants.MESSAGE_ACCOUNT_NOT_FOUND));
    }

    private static String validateAndNormalizeTema(String temaBruto) {
        if (temaBruto == null) {
            throw new AccountException(TopicValidationConstants.MESSAGE_TEMA_REQUIRED);
        }
        String trimmed = temaBruto.trim();
        if (trimmed.isEmpty()) {
            throw new AccountException(TopicValidationConstants.MESSAGE_TEMA_REQUIRED);
        }
        if (trimmed.length() < TopicValidationConstants.MIN_TEMA_CHAR_COUNT_AFTER_TRIM) {
            throw new AccountException(TopicValidationConstants.MESSAGE_TEMA_INVALID_LENGTH);
        }
        return trimmed;
    }

    private static String normalizeContexto(String contextoBruto) {
        if (contextoBruto == null) {
            return null;
        }
        String trimmed = contextoBruto.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }
}
