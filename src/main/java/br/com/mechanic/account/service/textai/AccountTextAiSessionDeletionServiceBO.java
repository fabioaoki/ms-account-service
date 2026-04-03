package br.com.mechanic.account.service.textai;

public interface AccountTextAiSessionDeletionServiceBO {

    void softDeleteByAccountIdAndSessionId(Long accountId, Long textAiSessionId);
}
