package br.com.mechanic.account.service.openai;

public interface OpenAiAssistantReviewPort {

    OpenAiAssistantReviewResult review(String firstAssistantRawResponseJson);
}
