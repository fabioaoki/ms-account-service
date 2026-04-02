package br.com.mechanic.account.service.textai;

import br.com.mechanic.account.service.request.TextAiAssistantRequest;
import br.com.mechanic.account.service.response.TextAiAssistantResponse;

public interface TextAiAssistantServiceBO {

    TextAiAssistantResponse process(Long accountId, TextAiAssistantRequest request);
}
