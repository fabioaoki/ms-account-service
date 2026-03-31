package br.com.mechanic.account.service.topic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TopicAiConsolidationAsyncProcessor {

    private final TopicAiConsolidationService topicAiConsolidationService;

    @Async
    public void process(Long accountId, Long topicId) {
        try {
            topicAiConsolidationService.processQueuedConsolidation(accountId, topicId);
        } catch (Exception ex) {
            log.error("Async topic AI consolidation failed. accountId={} topicId={}", accountId, topicId, ex);
        }
    }
}
