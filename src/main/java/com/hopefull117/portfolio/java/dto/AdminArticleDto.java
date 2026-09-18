package com.hopefull117.portfolio.java.dto;

import java.time.Instant;
import java.util.List;

public record AdminArticleDto(String id, String title, String slug, String excerpt, String content, String coverImage, List<String> tags, boolean published, Instant createdAt, Instant updatedAt) {
}
