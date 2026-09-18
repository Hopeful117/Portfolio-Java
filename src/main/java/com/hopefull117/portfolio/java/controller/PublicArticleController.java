package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.dto.PublicArticleDetailDto;
import com.hopefull117.portfolio.java.dto.PublicArticleSummaryDto;
import com.hopefull117.portfolio.java.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/articles")
@RequiredArgsConstructor
public class PublicArticleController {
    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<List<PublicArticleSummaryDto>> list() {
        return ResponseEntity.ok(articleService.findPublicSummaries());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PublicArticleDetailDto> detail(@PathVariable String slug) {
        return ResponseEntity.ok(articleService.findPublicDetailBySlug(slug));
    }
}
