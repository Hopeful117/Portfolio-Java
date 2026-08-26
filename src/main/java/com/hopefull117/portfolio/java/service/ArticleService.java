package com.hopefull117.portfolio.java.service;

import com.hopefull117.portfolio.java.dto.ArticleViewDto;
import com.hopefull117.portfolio.java.exception.ArticlePersistenceException;
import com.hopefull117.portfolio.java.exception.ArticleSlugConflictException;
import com.hopefull117.portfolio.java.exception.EntityNotFoundException;
import com.hopefull117.portfolio.java.model.Article;
import com.hopefull117.portfolio.java.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {

    private static final int MAX_SLUG_ATTEMPTS = 1000;

    private final ArticleRepository articleRepository;
    private final FileStorageService fileStorageService;
    private final MarkdownService markdownService;
    private final SlugGenerator slugGenerator;
    private final ArticleImageProcessor articleImageProcessor;

    public List<Article> getAll() {
        return articleRepository.findAll();
    }

    public Article findById(String id) {
        return articleRepository.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Article non trouvé")
                );
    }

    public Article create(Article article, MultipartFile file) throws IOException {
        String baseSlug = slugGenerator.generate(article.getTitle());
        int candidateNumber;

        try {
            candidateNumber = findAvailableCandidateNumber(baseSlug, 1);
        } catch (DataAccessException exception) {
            throw new ArticlePersistenceException("Impossible d'enregistrer l'article pour le moment", exception);
        }

        article.setId(null);
        article.setSlug(slugCandidate(baseSlug, candidateNumber));

        String coverUrl = null;
        if (!file.isEmpty()) {
            ProcessedImage processed = articleImageProcessor.process(file);
            coverUrl = fileStorageService.saveArticleWebP(processed.data());
            article.setCoverImage(coverUrl);
        }

        Instant now = Instant.now();
        article.setCreatedAt(now);
        article.setUpdatedAt(now);

        while (candidateNumber <= MAX_SLUG_ATTEMPTS) {
            try {
                return articleRepository.save(article);
            } catch (DuplicateKeyException exception) {
                candidateNumber++;
                try {
                    candidateNumber = findAvailableCandidateNumber(baseSlug, candidateNumber);
                    article.setSlug(slugCandidate(baseSlug, candidateNumber));
                } catch (DataAccessException dataAccessException) {
                    cleanupOwnedAsset(coverUrl);
                    throw new ArticlePersistenceException(
                            "Impossible d'enregistrer l'article pour le moment",
                            dataAccessException
                    );
                }
            } catch (DataAccessException exception) {
                cleanupOwnedAsset(coverUrl);
                throw new ArticlePersistenceException("Impossible d'enregistrer l'article pour le moment", exception);
            }
        }

        cleanupOwnedAsset(coverUrl);
        throw slugConflict();
    }

    public void update(String id, Article article, MultipartFile file) throws IOException {
        Article existingArticle = findById(id);
        String oldCover = existingArticle.getCoverImage();

        String newCoverUrl = null;
        if (!file.isEmpty()) {
            ProcessedImage processed = articleImageProcessor.process(file);
            newCoverUrl = fileStorageService.saveArticleWebP(processed.data());
            existingArticle.setCoverImage(newCoverUrl);
        }

        existingArticle.setTitle(article.getTitle());
        existingArticle.setExcerpt(article.getExcerpt());
        existingArticle.setContent(article.getContent());
        existingArticle.setTags(article.getTags());
        existingArticle.setPublished(article.isPublished());
        existingArticle.setUpdatedAt(Instant.now());

        try {
            articleRepository.save(existingArticle);
        } catch (DataAccessException exception) {
            cleanupOwnedAsset(newCoverUrl);
            throw new ArticlePersistenceException("Impossible d'enregistrer l'article pour le moment", exception);
        }

        if (newCoverUrl != null && fileStorageService.isArticleOwned(oldCover)) {
            fileStorageService.deleteArticleAsset(oldCover);
        }
    }

    public void deleteById(String id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé"));

        String coverImage = article.getCoverImage();
        boolean isOwned = fileStorageService.isArticleOwned(coverImage);

        articleRepository.deleteById(id);

        if (isOwned) {
            fileStorageService.deleteArticleAsset(coverImage);
        }
    }

    public List<Article> findPublished() {
        return articleRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }

    public ArticleViewDto findBySlug(String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(
                        () -> new EntityNotFoundException("Article non trouvé")
                );

        return ArticleViewDto.builder()
                .title(article.getTitle())
                .slug(article.getSlug())
                .excerpt(article.getExcerpt())
                .content(markdownService.toHtml(article.getContent()))
                .coverImage(article.getCoverImage())
                .tags(article.getTags())
                .createdAt(article.getCreatedAt())
                .build();
    }

    private void cleanupOwnedAsset(String coverUrl) {
        if (coverUrl != null && fileStorageService.isArticleOwned(coverUrl)) {
            try {
                fileStorageService.deleteArticleAsset(coverUrl);
            } catch (Exception e) {
                log.warn("Échec du nettoyage de l'asset {}: {}", coverUrl, e.getMessage());
            }
        }
    }

    private int findAvailableCandidateNumber(String baseSlug, int start) {
        for (int candidateNumber = start; candidateNumber <= MAX_SLUG_ATTEMPTS; candidateNumber++) {
            if (!articleRepository.existsBySlug(slugCandidate(baseSlug, candidateNumber))) {
                return candidateNumber;
            }
        }

        throw slugConflict();
    }

    private String slugCandidate(String baseSlug, int candidateNumber) {
        if (candidateNumber == 1) {
            return baseSlug;
        }

        String suffix = "-" + candidateNumber;
        int maximumBaseLength = SlugGenerator.MAX_LENGTH - suffix.length();
        String boundedBase = baseSlug.substring(0, Math.min(baseSlug.length(), maximumBaseLength))
                .replaceAll("-+$", "");
        return boundedBase + suffix;
    }

    private ArticleSlugConflictException slugConflict() {
        return new ArticleSlugConflictException(
                "Impossible de générer une URL unique pour cet article"
        );
    }
}
