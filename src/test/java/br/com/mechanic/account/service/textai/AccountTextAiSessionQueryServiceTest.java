package br.com.mechanic.account.service.textai;

import br.com.mechanic.account.constant.TextAiAssistantValidationConstants;
import br.com.mechanic.account.constant.TopicPaginationConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.textai.AccountTextAiSession;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import br.com.mechanic.account.exception.AccountException;
import br.com.mechanic.account.repository.account.jpa.AccountRepositoryJpa;
import br.com.mechanic.account.repository.textai.jpa.AccountTextAiSessionRepositoryJpa;
import br.com.mechanic.account.security.ApiAccessValidation;
import br.com.mechanic.account.service.response.AccountTextAiSessionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountTextAiSessionQueryServiceTest {

    @Mock
    private ApiAccessValidation apiAccessValidation;

    @Mock
    private AccountRepositoryJpa accountRepositoryJpa;

    @Mock
    private AccountTextAiSessionRepositoryJpa sessionRepositoryJpa;

    private AccountTextAiSessionQueryService service;

    @BeforeEach
    void setUp() {
        service = new AccountTextAiSessionQueryService(
                apiAccessValidation,
                accountRepositoryJpa,
                sessionRepositoryJpa
        );
    }

    @Test
    @DisplayName("page negativo: lança AccountException")
    void negativePageThrows() {
        doNothing().when(apiAccessValidation).requireTextAiAssistantAccess(1L);
        Account account = Account.builder().id(1L).status(AccountStatusEnum.ACTIVE).build();
        when(accountRepositoryJpa.findById(1L)).thenReturn(Optional.of(account));

        AccountException ex = assertThrows(
                AccountException.class,
                () -> service.listByAccountId(1L, -1, TopicPaginationConstants.DEFAULT_PAGE_SIZE)
        );
        assertEquals(TextAiAssistantValidationConstants.MESSAGE_TEXT_AI_SESSION_PAGE_NUMBER_NEGATIVE, ex.getMessage());
    }

    @Test
    @DisplayName("getByIdAndAccountId: sessão inexistente lança AccountException")
    void getByIdWhenMissingThrows() {
        long accountId = 2L;
        doNothing().when(apiAccessValidation).requireTextAiAssistantAccess(accountId);
        Account account = Account.builder().id(accountId).status(AccountStatusEnum.ACTIVE).build();
        when(accountRepositoryJpa.findById(accountId)).thenReturn(Optional.of(account));
        when(sessionRepositoryJpa.findByIdAndAccount_IdAndIsDeletedIsNull(99L, accountId)).thenReturn(Optional.empty());

        AccountException ex = assertThrows(
                AccountException.class,
                () -> service.getByIdAndAccountId(accountId, 99L)
        );
        assertEquals(TextAiAssistantValidationConstants.MESSAGE_TEXT_AI_SESSION_NOT_FOUND_FOR_ACCOUNT, ex.getMessage());
    }

    @Test
    @DisplayName("getByIdAndAccountId: retorna resposta quando encontra")
    void getByIdReturnsResponse() {
        long accountId = 3L;
        doNothing().when(apiAccessValidation).requireTextAiAssistantAccess(accountId);
        Account account = Account.builder()
                .id(accountId)
                .publicId(UUID.randomUUID())
                .status(AccountStatusEnum.ACTIVE)
                .build();
        when(accountRepositoryJpa.findById(accountId)).thenReturn(Optional.of(account));

        AccountTextAiSession session = AccountTextAiSession.builder()
                .id(10L)
                .account(account)
                .openAiThreadId("thread_x")
                .title("Título")
                .resume("Resumo")
                .timeConsidered(true)
                .expectedMinutes(30)
                .createdAt(LocalDateTime.parse("2026-04-02T10:00:00"))
                .lastUpdatedAt(null)
                .build();
        when(sessionRepositoryJpa.findByIdAndAccount_IdAndIsDeletedIsNull(10L, accountId)).thenReturn(Optional.of(session));

        AccountTextAiSessionResponse response = service.getByIdAndAccountId(accountId, 10L);

        assertEquals(10L, response.id());
        assertEquals(accountId, response.accountId());
        assertEquals("thread_x", response.openAiThreadId());
        assertEquals("Título", response.title());
    }

    @Test
    @DisplayName("getByIdAndAccountId: sessão soft-deleted não é retornada")
    void getByIdWhenSoftDeletedThrows() {
        long accountId = 4L;
        doNothing().when(apiAccessValidation).requireTextAiAssistantAccess(accountId);
        Account account = Account.builder().id(accountId).status(AccountStatusEnum.ACTIVE).build();
        when(accountRepositoryJpa.findById(accountId)).thenReturn(Optional.of(account));
        when(sessionRepositoryJpa.findByIdAndAccount_IdAndIsDeletedIsNull(20L, accountId)).thenReturn(Optional.empty());

        AccountException ex = assertThrows(
                AccountException.class,
                () -> service.getByIdAndAccountId(accountId, 20L)
        );
        assertEquals(TextAiAssistantValidationConstants.MESSAGE_TEXT_AI_SESSION_NOT_FOUND_FOR_ACCOUNT, ex.getMessage());
    }
}
