package com.hopefull117.portfolio.java.dto;

import java.time.Instant;
import java.util.List;

public record PublicArticleSummaryDto(
        String title,
        String slug,
        String excerpt,
        String coverImage,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt
) {
    public PublicArticleSummaryDto {
        tags = List.copyOf(tags == null ? List.of() : tags);
    }
}
