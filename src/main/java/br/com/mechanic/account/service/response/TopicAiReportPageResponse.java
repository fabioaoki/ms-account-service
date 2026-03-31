package br.com.mechanic.account.service.response;

import java.util.List;

public record TopicAiReportPageResponse(
        List<TopicAiReportResponse> content,
        long totalElements,
        int totalPages,
        int size,
        int number,
        boolean first,
        boolean last
) {
}
