package com.hopefull117.portfolio.java.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ArticleViewDto {

    private String title;

    private String slug;

    private String excerpt;

    private String content;

    private String coverImage;

    private List<String> tags;

    private Instant createdAt;

}