package com.hopefull117.portfolio.java.service;

import dev.matrixlab.webp4j.WebPCodec;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class ArticleImageProcessor {

    private static final long MAX_PIXELS = 25_000_000L;
    private static final int MAX_WIDTH = 1200;
    private static final float WEBP_QUALITY = 80.0f;

    private static final String MIME_PNG = "image/png";
    private static final String MIME_JPEG = "image/jpeg";
    private static final String MIME_WEBP = "image/webp";

    public ProcessedImage process(MultipartFile file) throws IOException {
        validateMimeType(file.getContentType());
        validateFileSize(file.getSize());

        byte[] bytes = file.getBytes();
        BufferedImage image = decodeImage(bytes, file.getContentType());
        validateDimensions(image.getWidth(), image.getHeight());

        BufferedImage resized = resizeIfOversized(image);
        byte[] webpData = encodeWebP(resized);

        return new ProcessedImage(webpData, resized.getWidth(), resized.getHeight());
    }

    private void validateMimeType(String contentType) {
        if (contentType == null || (!contentType.equals(MIME_PNG)
                && !contentType.equals(MIME_JPEG)
                && !contentType.equals(MIME_WEBP))) {
            throw new IllegalArgumentException(
                    "Format d'image non supporté. Formats acceptés : PNG, JPEG, WebP");
        }
    }

    private void validateFileSize(long size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Le fichier est vide");
        }
        if (size > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "Le fichier est trop volumineux. Taille maximale : 5 Mo");
        }
    }

    private BufferedImage decodeImage(byte[] bytes, String contentType) throws IOException {
        BufferedImage image;

        if (MIME_WEBP.equals(contentType)) {
            if (!WebPCodec.isAvailable()) {
                throw new IOException(
                        "Le codec WebP n'est pas disponible sur cette plateforme");
            }
            image = WebPCodec.decodeImage(bytes);
        } else if (MIME_JPEG.equals(contentType)) {
            image = Thumbnails.of(new ByteArrayInputStream(bytes))
                    .scale(1.0)
                    .asBufferedImage();
        } else {
            image = ImageIO.read(new ByteArrayInputStream(bytes));
        }

        if (image == null) {
            throw new IllegalArgumentException(
                    "Image invalide ou corrompue");
        }

        return image;
    }

    private void validateDimensions(int width, int height) {
        long pixels = (long) width * height;
        if (pixels > MAX_PIXELS) {
            throw new IllegalArgumentException(
                    "Les dimensions de l'image sont trop importantes. Maximum : 25 mégapixels");
        }
    }

    private BufferedImage resizeIfOversized(BufferedImage image) throws IOException {
        if (image.getWidth() <= MAX_WIDTH) {
            return image;
        }

        return Thumbnails.of(image)
                .width(MAX_WIDTH)
                .keepAspectRatio(true)
                .outputFormat("png")
                .asBufferedImage();
    }

    private byte[] encodeWebP(BufferedImage image) throws IOException {
        if (!WebPCodec.isAvailable()) {
            throw new IOException(
                    "Le codec WebP n'est pas disponible sur cette plateforme");
        }
        return WebPCodec.encodeImage(image, WEBP_QUALITY);
    }
}
