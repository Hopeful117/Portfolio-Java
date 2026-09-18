package com.hopefull117.portfolio.java.dto;

import java.time.Instant;
import java.util.List;

public record PublicArticleDetailDto(
        String title,
        String slug,
        String excerpt,
        String coverImage,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt,
        String renderedHtml,
        List<TableOfContentsEntry> tableOfContents
) {
    public PublicArticleDetailDto {
        tags = List.copyOf(tags == null ? List.of() : tags);
        tableOfContents = List.copyOf(tableOfContents == null ? List.of() : tableOfContents);
    }
}
