package com.hopefull117.portfolio.java.service;

import com.hopefull117.portfolio.java.exception.ArticlePersistenceException;
import com.hopefull117.portfolio.java.exception.ArticleSlugConflictException;
import com.hopefull117.portfolio.java.model.Article;
import com.hopefull117.portfolio.java.repository.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private MarkdownService markdownService;
    @Mock
    private ArticleImageProcessor articleImageProcessor;

    private ArticleService articleService;
    private MockMultipartFile emptyImage;

    @BeforeEach
    void setUp() {
        articleService = new ArticleService(
                articleRepository,
                fileStorageService,
                markdownService,
                new SlugGenerator(),
                articleImageProcessor
        );
        emptyImage = new MockMultipartFile("image", new byte[0]);
    }

    @Test
    void createGeneratesAndPersistsSlugWhilePreservingArticleData() throws IOException {
        Article article = Article.builder()
                .id("submitted-id")
                .title("Java & Spring Boot")
                .slug("manual-override")
                .excerpt("Résumé")
                .content("# Contenu")
                .tags(List.of("Java", "Spring"))
                .published(true)
                .build();
        when(articleRepository.existsBySlug("java-spring-boot")).thenReturn(false);
        when(articleRepository.save(article)).thenReturn(article);

        Article saved = articleService.create(article, emptyImage);

        assertSame(article, saved);
        assertEquals("java-spring-boot", saved.getSlug());
        assertNull(saved.getId());
        assertEquals("Résumé", saved.getExcerpt());
        assertEquals("# Contenu", saved.getContent());
        assertEquals(List.of("Java", "Spring"), saved.getTags());
        assertTrue(saved.isPublished());
        assertNotNull(saved.getCreatedAt());
        assertEquals(saved.getCreatedAt(), saved.getUpdatedAt());
        verify(articleRepository).save(article);
        verify(fileStorageService, never()).saveArticleWebP(any());
    }

    @Test
    void successiveCollisionsUseTwoAndThree() throws IOException {
        Set<String> persistedSlugs = new HashSet<>();
        when(articleRepository.existsBySlug(anyString()))
                .thenAnswer(invocation -> persistedSlugs.contains(invocation.getArgument(0)));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article article = invocation.getArgument(0);
            persistedSlugs.add(article.getSlug());
            return article;
        });

        Article first = articleService.create(article("Spring Security"), emptyImage);
        Article second = articleService.create(article("Spring Security"), emptyImage);
        Article third = articleService.create(article("Spring Security"), emptyImage);

        assertEquals("spring-security", first.getSlug());
        assertEquals("spring-security-2", second.getSlug());
        assertEquals("spring-security-3", third.getSlug());
    }

    @Test
    void duplicateKeyRaceRetriesWithNextSuffix() throws IOException {
        Article article = article("Concurrent title");
        when(articleRepository.existsBySlug(anyString())).thenReturn(false);
        when(articleRepository.save(article))
                .thenThrow(new DuplicateKeyException("duplicate"))
                .thenReturn(article);

        Article saved = articleService.create(article, emptyImage);

        assertEquals("concurrent-title-2", saved.getSlug());
    }

    @Test
    void collisionSearchIsBounded() throws IOException {
        when(articleRepository.existsBySlug(anyString())).thenReturn(true);

        assertThrows(
                ArticleSlugConflictException.class,
                () -> articleService.create(article("Same title"), emptyImage)
        );
        verify(articleRepository, never()).save(any(Article.class));
        verify(fileStorageService, never()).saveArticleWebP(any());
    }

    @Test
    void createWithImageProcessesAndStoresWebP() throws IOException {
        Article article = article("Cover image");
        MockMultipartFile image = new MockMultipartFile("image", "cover.png", "image/png", new byte[]{1, 2, 3});
        ProcessedImage processed = new ProcessedImage(new byte[]{4, 5, 6}, 800, 600);

        when(articleRepository.existsBySlug("cover-image")).thenReturn(false);
        when(articleImageProcessor.process(image)).thenReturn(processed);
        when(fileStorageService.saveArticleWebP(processed.data())).thenReturn("/uploads/articles/abc.webp");
        when(articleRepository.save(article)).thenReturn(article);

        Article saved = articleService.create(article, image);

        assertEquals("/uploads/articles/abc.webp", saved.getCoverImage());
        verify(articleImageProcessor).process(image);
        verify(fileStorageService).saveArticleWebP(processed.data());
    }

    @Test
    void createWithImageCleansUpOnPersistenceFailure() throws IOException {
        Article article = article("Cover image");
        MockMultipartFile image = new MockMultipartFile("image", "cover.png", "image/png", new byte[]{1});
        ProcessedImage processed = new ProcessedImage(new byte[]{4, 5, 6}, 800, 600);

        when(articleRepository.existsBySlug("cover-image")).thenReturn(false);
        when(articleImageProcessor.process(image)).thenReturn(processed);
        when(fileStorageService.saveArticleWebP(processed.data())).thenReturn("/uploads/articles/abc.webp");
        when(fileStorageService.isArticleOwned("/uploads/articles/abc.webp")).thenReturn(true);
        when(articleRepository.save(article)).thenThrow(new DataAccessResourceFailureException("mongo down"));

        assertThrows(ArticlePersistenceException.class,
                () -> articleService.create(article, image));

        verify(fileStorageService).deleteArticleAsset("/uploads/articles/abc.webp");
    }

    @Test
    void createWithImageCleansUpWhenRetryLookupFails() throws IOException {
        Article article = article("Cover image");
        MockMultipartFile image = new MockMultipartFile("image", "cover.png", "image/png", new byte[]{1});
        ProcessedImage processed = new ProcessedImage(new byte[]{4, 5, 6}, 800, 600);

        when(articleRepository.existsBySlug(anyString()))
                .thenReturn(false)
                .thenThrow(new DataAccessResourceFailureException("mongo down"));
        when(articleImageProcessor.process(image)).thenReturn(processed);
        when(fileStorageService.saveArticleWebP(processed.data())).thenReturn("/uploads/articles/abc.webp");
        when(fileStorageService.isArticleOwned("/uploads/articles/abc.webp")).thenReturn(true);
        when(articleRepository.save(article)).thenThrow(new DuplicateKeyException("duplicate"));

        assertThrows(ArticlePersistenceException.class,
                () -> articleService.create(article, image));

        verify(fileStorageService).deleteArticleAsset("/uploads/articles/abc.webp");
    }

    @Test
    void updatePreservesLegacySlugCreatedAtAndCoverWhileUpdatingEditableFields() throws IOException {
        Instant createdAt = Instant.parse("2025-01-01T10:00:00Z");
        Article existing = Article.builder()
                .id("article-id")
                .title("Old title")
                .slug("Old_Article-Slug")
                .excerpt("Old excerpt")
                .content("Old content")
                .coverImage("/uploads/projects/old.png")
                .tags(List.of("Old"))
                .published(false)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
        Article changes = Article.builder()
                .title("New title")
                .slug("malicious-change")
                .excerpt("New excerpt")
                .content("New content")
                .tags(List.of("New"))
                .published(true)
                .build();
        when(articleRepository.findById("article-id")).thenReturn(java.util.Optional.of(existing));

        articleService.update("article-id", changes, emptyImage);

        assertEquals("New title", existing.getTitle());
        assertEquals("Old_Article-Slug", existing.getSlug());
        assertEquals("New excerpt", existing.getExcerpt());
        assertEquals("New content", existing.getContent());
        assertEquals(List.of("New"), existing.getTags());
        assertTrue(existing.isPublished());
        assertEquals("/uploads/projects/old.png", existing.getCoverImage());
        assertEquals(createdAt, existing.getCreatedAt());
        assertTrue(existing.getUpdatedAt().isAfter(createdAt));
        verify(fileStorageService, never()).saveArticleWebP(any());
        verify(articleRepository).save(existing);
    }

    @Test
    void updateWithNewImageStoresWebPAndCleansOldOwnedFile() throws IOException {
        Instant createdAt = Instant.parse("2025-01-01T10:00:00Z");
        Article existing = Article.builder()
                .id("article-id")
                .slug("stable-slug")
                .coverImage("/uploads/articles/old.webp")
                .createdAt(createdAt)
                .build();
        Article changes = Article.builder().title("Changed").build();
        MockMultipartFile image = new MockMultipartFile("image", "new.png", "image/png", new byte[]{1});
        ProcessedImage processed = new ProcessedImage(new byte[]{4, 5, 6}, 800, 600);

        when(articleRepository.findById("article-id")).thenReturn(java.util.Optional.of(existing));
        when(articleImageProcessor.process(image)).thenReturn(processed);
        when(fileStorageService.saveArticleWebP(processed.data())).thenReturn("/uploads/articles/new.webp");
        when(fileStorageService.isArticleOwned("/uploads/articles/old.webp")).thenReturn(true);

        articleService.update("article-id", changes, image);

        assertEquals("stable-slug", existing.getSlug());
        assertEquals("/uploads/articles/new.webp", existing.getCoverImage());
        verify(fileStorageService).deleteArticleAsset("/uploads/articles/old.webp");
    }

    @Test
    void updateWithNewImageDoesNotDeleteLegacyFile() throws IOException {
        Article existing = Article.builder()
                .id("article-id")
                .slug("stable-slug")
                .coverImage("/uploads/projects/legacy.png")
                .build();
        Article changes = Article.builder().title("Changed").build();
        MockMultipartFile image = new MockMultipartFile("image", "new.png", "image/png", new byte[]{1});
        ProcessedImage processed = new ProcessedImage(new byte[]{4, 5, 6}, 800, 600);

        when(articleRepository.findById("article-id")).thenReturn(java.util.Optional.of(existing));
        when(articleImageProcessor.process(image)).thenReturn(processed);
        when(fileStorageService.saveArticleWebP(processed.data())).thenReturn("/uploads/articles/new.webp");
        when(fileStorageService.isArticleOwned("/uploads/projects/legacy.png")).thenReturn(false);

        articleService.update("article-id", changes, image);

        assertEquals("/uploads/articles/new.webp", existing.getCoverImage());
        verify(fileStorageService, never()).deleteArticleAsset("/uploads/projects/legacy.png");
    }

    @Test
    void updateWithImageFailurePreservesOldCover() throws IOException {
        Article existing = Article.builder()
                .id("article-id")
                .coverImage("/uploads/articles/old.webp")
                .build();
        Article changes = Article.builder().title("Changed").build();
        MockMultipartFile image = new MockMultipartFile("image", "new.png", "image/png", new byte[]{1});

        when(articleRepository.findById("article-id")).thenReturn(java.util.Optional.of(existing));
        when(articleImageProcessor.process(image)).thenThrow(new IOException("decode failed"));

        assertThrows(IOException.class,
                () -> articleService.update("article-id", changes, image));

        assertEquals("/uploads/articles/old.webp", existing.getCoverImage());
    }

    @Test
    void deleteByIdWithOwnedCoverDeletesFileAfterDbDelete() throws IOException {
        Article article = Article.builder()
                .id("article-id")
                .coverImage("/uploads/articles/cover.webp")
                .build();
        when(articleRepository.findById("article-id")).thenReturn(java.util.Optional.of(article));
        when(fileStorageService.isArticleOwned("/uploads/articles/cover.webp")).thenReturn(true);

        articleService.deleteById("article-id");

        verify(articleRepository).deleteById("article-id");
        verify(fileStorageService).deleteArticleAsset("/uploads/articles/cover.webp");
    }

    @Test
    void deleteByIdWithLegacyCoverDoesNotDeleteFile() throws IOException {
        Article article = Article.builder()
                .id("article-id")
                .coverImage("/uploads/projects/legacy.png")
                .build();
        when(articleRepository.findById("article-id")).thenReturn(java.util.Optional.of(article));
        when(fileStorageService.isArticleOwned("/uploads/projects/legacy.png")).thenReturn(false);

        articleService.deleteById("article-id");

        verify(articleRepository).deleteById("article-id");
        verify(fileStorageService, never()).deleteArticleAsset("/uploads/projects/legacy.png");
    }

    @Test
    void deleteByIdWithNoCoverDoesNotAttemptCleanup() throws IOException {
        Article article = Article.builder()
                .id("article-id")
                .coverImage(null)
                .build();
        when(articleRepository.findById("article-id")).thenReturn(java.util.Optional.of(article));
        when(fileStorageService.isArticleOwned(null)).thenReturn(false);

        articleService.deleteById("article-id");

        verify(articleRepository).deleteById("article-id");
        verify(fileStorageService, never()).deleteArticleAsset(any());
    }

    @Test
    void persistenceFailureIsTranslated() {
        Article article = article("Failure");
        when(articleRepository.existsBySlug("failure"))
                .thenThrow(new DataAccessResourceFailureException("mongo unavailable"));

        ArticlePersistenceException exception = assertThrows(
                ArticlePersistenceException.class,
                () -> articleService.create(article, emptyImage)
        );

        assertFalse(exception.getMessage().contains("mongo"));
    }

    private Article article(String title) {
        return Article.builder().title(title).build();
    }
}
