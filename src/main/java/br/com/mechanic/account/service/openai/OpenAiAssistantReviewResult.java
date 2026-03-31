package br.com.mechanic.account.service.openai;

public record OpenAiAssistantReviewResult(boolean accept, String problematicText) {
}
