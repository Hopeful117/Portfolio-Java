package com.hopefull117.portfolio.java.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    private static final String ARTICLE_URL_PREFIX = "/uploads/articles/";

    private final Path root;
    private final Path articleRoot;

    public FileStorageService() {
        this(Paths.get("uploads"));
    }

    FileStorageService(Path uploadsRoot) {
        this.root = uploadsRoot.resolve("projects");
        this.articleRoot = uploadsRoot.resolve("articles");
    }

    public String save(MultipartFile file) throws IOException {

        Files.createDirectories(root);

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

        Files.copy(file.getInputStream(),
                root.resolve(filename));

        return "/uploads/projects/" + filename;
    }

    public String saveArticleWebP(byte[] data) throws IOException {
        Files.createDirectories(articleRoot);

        String filename = UUID.randomUUID() + ".webp";
        Files.write(articleRoot.resolve(filename), data);

        return ARTICLE_URL_PREFIX + filename;
    }

    public void deleteArticleAsset(String publicPath) {
        if (!isArticleOwned(publicPath)) {
            return;
        }

        String filename = publicPath.substring(ARTICLE_URL_PREFIX.length());
        Path normalizedRoot = articleRoot.toAbsolutePath().normalize();
        Path filePath = normalizedRoot.resolve(filename).normalize();

        if (!normalizedRoot.equals(filePath.getParent())) {
            return;
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Échec du nettoyage de l'asset article {}", publicPath, e);
        }
    }

    public boolean isArticleOwned(String publicPath) {
        if (publicPath == null || !publicPath.startsWith(ARTICLE_URL_PREFIX)) {
            return false;
        }

        String filename = publicPath.substring(ARTICLE_URL_PREFIX.length());
        if (!filename.endsWith(".webp") || filename.contains("/") || filename.contains("\\")) {
            return false;
        }

        try {
            UUID.fromString(filename.substring(0, filename.length() - ".webp".length()));
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
