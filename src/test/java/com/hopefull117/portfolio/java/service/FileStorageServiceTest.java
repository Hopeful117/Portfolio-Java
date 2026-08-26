package com.hopefull117.portfolio.java.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTest {

    private FileStorageService service;
    private Path tempUploadsDir;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempUploadsDir = tempDir.resolve("uploads");
        service = new FileStorageService(tempUploadsDir);
        Files.createDirectories(tempUploadsDir);
    }

    @AfterEach
    void tearDown() throws IOException {
        try (Stream<Path> paths = Files.walk(tempUploadsDir)) {
            paths.sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try { Files.deleteIfExists(path); } catch (IOException ignored) {}
                    });
        }
    }

    @Test
    void saveArticleWebPCreatesFileInArticlesDirectory() throws IOException {
        byte[] webpData = "fake-webp-data".getBytes();
        String url = service.saveArticleWebP(webpData);

        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/articles/"));
        assertTrue(url.endsWith(".webp"));
        Path storedFile = storedFile(url);
        assertTrue(Files.exists(storedFile));
        assertArrayEquals(webpData, Files.readAllBytes(storedFile));
    }

    @Test
    void saveArticleWebPReturnsUniqueUrls() throws IOException {
        byte[] data = "data".getBytes();
        String url1 = service.saveArticleWebP(data);
        String url2 = service.saveArticleWebP(data);

        assertFalse(url1.equals(url2));
    }

    @Test
    void deleteArticleAssetRemovesOwnedFile() throws IOException {
        byte[] data = "test".getBytes();
        String url = service.saveArticleWebP(data);
        Path storedFile = storedFile(url);

        assertDoesNotThrow(() -> service.deleteArticleAsset(url));
        assertFalse(Files.exists(storedFile));
    }

    @Test
    void deleteArticleAssetIgnoresNull() {
        assertDoesNotThrow(() -> service.deleteArticleAsset(null));
    }

    @Test
    void deleteArticleAssetIgnoresLegacyPath() throws IOException {
        Path legacyFile = tempUploadsDir.resolve("projects/old.png");
        Files.createDirectories(legacyFile.getParent());
        Files.write(legacyFile, new byte[]{1});

        assertDoesNotThrow(() -> service.deleteArticleAsset("/uploads/projects/old.png"));
        assertTrue(Files.exists(legacyFile));
    }

    @Test
    void deleteArticleAssetIgnoresTraversal() throws IOException {
        Path outsideFile = tempDir.resolve("etc/passwd");
        Files.createDirectories(outsideFile.getParent());
        Files.write(outsideFile, new byte[]{1});

        assertDoesNotThrow(() -> service.deleteArticleAsset("/uploads/articles/../../etc/passwd"));
        assertTrue(Files.exists(outsideFile));
    }

    @Test
    void isArticleOwnedReturnsTrueForArticlePath() {
        assertTrue(service.isArticleOwned("/uploads/articles/" + UUID.randomUUID() + ".webp"));
    }

    @Test
    void isArticleOwnedReturnsFalseForLegacyPath() {
        assertFalse(service.isArticleOwned("/uploads/projects/old.png"));
    }

    @Test
    void isArticleOwnedReturnsFalseForNull() {
        assertFalse(service.isArticleOwned(null));
    }

    @Test
    void isArticleOwnedRejectsNonPipelineFilename() {
        assertFalse(service.isArticleOwned("/uploads/articles/abc.webp"));
    }

    private Path storedFile(String url) {
        return tempUploadsDir.resolve(url.substring("/uploads/".length()));
    }
}
