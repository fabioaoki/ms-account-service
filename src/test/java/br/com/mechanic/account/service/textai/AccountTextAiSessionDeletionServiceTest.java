package br.com.mechanic.account.service.textai;

import br.com.mechanic.account.constant.TextAiAssistantValidationConstants;
import br.com.mechanic.account.constant.TopicValidationConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.textai.AccountTextAiSession;
import br.com.mechanic.account.entity.textai.AccountTextAiSessionHistory;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import br.com.mechanic.account.exception.AccountException;
import br.com.mechanic.account.repository.account.jpa.AccountRepositoryJpa;
import br.com.mechanic.account.repository.textai.jpa.AccountTextAiSessionHistoryRepositoryJpa;
import br.com.mechanic.account.repository.textai.jpa.AccountTextAiSessionRepositoryJpa;
import br.com.mechanic.account.security.ApiAccessValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountTextAiSessionDeletionServiceTest {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    @Mock
    private ApiAccessValidation apiAccessValidation;

    @Mock
    private AccountRepositoryJpa accountRepositoryJpa;

    @Mock
    private AccountTextAiSessionRepositoryJpa sessionRepositoryJpa;

    @Mock
    private AccountTextAiSessionHistoryRepositoryJpa sessionHistoryRepositoryJpa;

    private AccountTextAiSessionDeletionService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-04-03T12:00:00Z"), ZONE);
        service = new AccountTextAiSessionDeletionService(
                apiAccessValidation,
                accountRepositoryJpa,
                sessionRepositoryJpa,
                sessionHistoryRepositoryJpa,
                clock
        );
    }

    @Test
    @DisplayName("soft delete: atualiza flags e persiste histórico")
    void softDeleteUpdatesSessionAndInsertsHistory() {
        long accountId = 8L;
        doNothing().when(apiAccessValidation).requireTextAiAssistantAccess(accountId);
        Account account = Account.builder()
                .id(accountId)
                .publicId(UUID.randomUUID())
                .status(AccountStatusEnum.ACTIVE)
                .build();
        when(accountRepositoryJpa.findById(accountId)).thenReturn(Optional.of(account));

        AccountTextAiSession session = AccountTextAiSession.builder()
                .id(100L)
                .account(account)
                .openAiThreadId("t1")
                .title("T")
                .resume("R")
                .timeConsidered(false)
                .expectedMinutes(5)
                .createdAt(LocalDateTime.of(2026, 4, 1, 9, 0))
                .lastUpdatedAt(null)
                .isDeleted(null)
                .build();
        when(sessionRepositoryJpa.findByIdAndAccount_Id(100L, accountId)).thenReturn(Optional.of(session));
        when(sessionRepositoryJpa.save(any(AccountTextAiSession.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sessionHistoryRepositoryJpa.save(any(AccountTextAiSessionHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        service.softDeleteByAccountIdAndSessionId(accountId, 100L);

        assertTrue(session.getIsDeleted());
        assertNotNull(session.getLastUpdatedAt());
        ArgumentCaptor<AccountTextAiSessionHistory> historyCaptor = ArgumentCaptor.forClass(AccountTextAiSessionHistory.class);
        verify(sessionHistoryRepositoryJpa).save(historyCaptor.capture());
        AccountTextAiSessionHistory row = historyCaptor.getValue();
        assertEquals(session, row.getTextAiSession());
        assertEquals(account, row.getAccount());
        assertEquals(session.getLastUpdatedAt(), row.getCreatedAt());
    }

    @Test
    @DisplayName("conta inativa: lança mesma regra dos GETs")
    void inactiveAccountThrows() {
        long accountId = 3L;
        doNothing().when(apiAccessValidation).requireTextAiAssistantAccess(accountId);
        Account account = Account.builder().id(accountId).status(AccountStatusEnum.INACTIVE).build();
        when(accountRepositoryJpa.findById(accountId)).thenReturn(Optional.of(account));

        AccountException ex = assertThrows(
                AccountException.class,
                () -> service.softDeleteByAccountIdAndSessionId(accountId, 1L)
        );
        assertEquals(TopicValidationConstants.MESSAGE_ACCOUNT_MUST_BE_ACTIVE_FOR_TOPIC_ENDPOINTS, ex.getMessage());
    }

    @Test
    @DisplayName("sessão já excluída: mesma mensagem que GET")
    void alreadyDeletedThrows() {
        long accountId = 5L;
        doNothing().when(apiAccessValidation).requireTextAiAssistantAccess(accountId);
        Account account = Account.builder().id(accountId).status(AccountStatusEnum.ACTIVE).build();
        when(accountRepositoryJpa.findById(accountId)).thenReturn(Optional.of(account));
        AccountTextAiSession session = AccountTextAiSession.builder()
                .id(2L)
                .account(account)
                .isDeleted(Boolean.TRUE)
                .build();
        when(sessionRepositoryJpa.findByIdAndAccount_Id(2L, accountId)).thenReturn(Optional.of(session));

        AccountException ex = assertThrows(
                AccountException.class,
                () -> service.softDeleteByAccountIdAndSessionId(accountId, 2L)
        );
        assertEquals(TextAiAssistantValidationConstants.MESSAGE_TEXT_AI_SESSION_NOT_FOUND_FOR_ACCOUNT, ex.getMessage());
    }

    @Test
    @DisplayName("vínculo accountId + sessionId inexistente: mesma mensagem")
    void wrongAccountThrows() {
        long accountId = 6L;
        doNothing().when(apiAccessValidation).requireTextAiAssistantAccess(accountId);
        Account account = Account.builder().id(accountId).status(AccountStatusEnum.ACTIVE).build();
        when(accountRepositoryJpa.findById(accountId)).thenReturn(Optional.of(account));
        when(sessionRepositoryJpa.findByIdAndAccount_Id(999L, accountId)).thenReturn(Optional.empty());

        AccountException ex = assertThrows(
                AccountException.class,
                () -> service.softDeleteByAccountIdAndSessionId(accountId, 999L)
        );
        assertEquals(TextAiAssistantValidationConstants.MESSAGE_TEXT_AI_SESSION_NOT_FOUND_FOR_ACCOUNT, ex.getMessage());
    }
}
