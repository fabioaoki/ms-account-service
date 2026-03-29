package br.com.mechanic.account.service.response;

import java.util.List;

public record TopicPageResponse(
        List<TopicResponse> content,
        long totalElements,
        int totalPages,
        int size,
        int number,
        boolean first,
        boolean last
) {
}
