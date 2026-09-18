package com.hopefull117.portfolio.java.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AdminArticleRequest(@NotBlank String title, String excerpt, String content, List<String> tags, boolean published) {
}
