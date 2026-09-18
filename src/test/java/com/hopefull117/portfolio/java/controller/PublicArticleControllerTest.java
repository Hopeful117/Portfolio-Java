package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.dto.PublicArticleDetailDto;
import com.hopefull117.portfolio.java.dto.PublicArticleSummaryDto;
import com.hopefull117.portfolio.java.dto.TableOfContentsEntry;
import com.hopefull117.portfolio.java.exception.EntityNotFoundException;
import com.hopefull117.portfolio.java.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PublicArticleControllerTest {
    @Mock
    private ArticleService articleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PublicArticleController(articleService))
                .setControllerAdvice(new PublicApiExceptionHandler())
                .build();
    }

    @Test
    void listsOnlyPublicArticleProjectionFields() throws Exception {
        when(articleService.findPublicSummaries()).thenReturn(List.of(
                new PublicArticleSummaryDto(
                        "Published", "published", "Summary", "/cover.webp", List.of("Java"),
                        Instant.parse("2025-01-01T10:00:00Z"), Instant.parse("2025-01-02T10:00:00Z"))));

        mockMvc.perform(get("/api/public/articles"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0].title").value("Published"))
                .andExpect(jsonPath("$[0].slug").value("published"))
                .andExpect(jsonPath("$[0].published").doesNotExist())
                .andExpect(jsonPath("$[0].id").doesNotExist());

        verify(articleService).findPublicSummaries();
    }

    @Test
    void returnsPublishedArticleDetailWithRenderedMarkdownContract() throws Exception {
        when(articleService.findPublicDetailBySlug("published")).thenReturn(
                new PublicArticleDetailDto(
                        "Published", "published", "Summary", null, List.of("Java"),
                        Instant.parse("2025-01-01T10:00:00Z"), Instant.parse("2025-01-02T10:00:00Z"),
                        "<h2 id=\"results\">Results</h2>",
                        List.of(new TableOfContentsEntry("results", "Results", 2))));

        mockMvc.perform(get("/api/public/articles/published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.renderedHtml").value("<h2 id=\"results\">Results</h2>"))
                .andExpect(jsonPath("$.tableOfContents[0].id").value("results"))
                .andExpect(jsonPath("$.tableOfContents[0].level").value(2))
                .andExpect(jsonPath("$.content").doesNotExist());

        verify(articleService).findPublicDetailBySlug("published");
    }

    @Test
    void hidesMissingAndDraftSlugsAsTheSameNotFoundProblem() throws Exception {
        when(articleService.findPublicDetailBySlug("draft-or-missing"))
                .thenThrow(new EntityNotFoundException("Article non trouvé"));

        mockMvc.perform(get("/api/public/articles/draft-or-missing"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail").value("The requested public resource was not found."))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Article"))));
    }

    @Test
    void doesNotExposeUnexpectedServiceDetails() throws Exception {
        when(articleService.findPublicDetailBySlug("failure"))
                .thenThrow(new IllegalStateException("database-internal-detail"));

        mockMvc.perform(get("/api/public/articles/failure"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("The public request could not be completed."))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("database-internal-detail"))));
    }
}
