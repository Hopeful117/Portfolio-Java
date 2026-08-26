package com.hopefull117.portfolio.java.service;

import dev.matrixlab.webp4j.WebPCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ArticleImageProcessorTest {

    private ArticleImageProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ArticleImageProcessor();
    }

    @Test
    void processJpegImageReturnsWebP() throws IOException {
        byte[] jpegBytes = createTestImage(800, 600, "jpg");
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", jpegBytes);

        ProcessedImage result = processor.process(file);

        assertNotNull(result);
        assertNotNull(result.data());
        assertTrue(result.data().length > 0);
        assertEquals(800, result.width());
        assertEquals(600, result.height());
    }

    @Test
    void processPngImageReturnsWebP() throws IOException {
        byte[] pngBytes = createTestImage(800, 600, "png");
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.png", "image/png", pngBytes);

        ProcessedImage result = processor.process(file);

        assertNotNull(result);
        assertNotNull(result.data());
        assertTrue(result.data().length > 0);
        assertEquals(800, result.width());
        assertEquals(600, result.height());
    }

    @Test
    void processWebPImageReturnsCanonicalWebP() throws IOException {
        if (!WebPCodec.isAvailable()) return;

        BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 800, 600);
        g.dispose();
        byte[] webpBytes = WebPCodec.encodeImage(image, 80.0f);

        MockMultipartFile file = new MockMultipartFile(
                "image", "test.webp", "image/webp", webpBytes);

        ProcessedImage result = processor.process(file);

        assertNotNull(result);
        assertNotNull(result.data());
        assertTrue(result.data().length > 0);
    }

    @Test
    void processPngWithTransparencyPreservesAlpha() throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(255, 0, 0, 128));
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] pngBytes = baos.toByteArray();

        MockMultipartFile file = new MockMultipartFile(
                "image", "alpha.png", "image/png", pngBytes);

        ProcessedImage result = processor.process(file);

        assertNotNull(result);
        assertTrue(result.data().length > 0);

        if (WebPCodec.isAvailable()) {
            BufferedImage decoded = WebPCodec.decodeImage(result.data());
            assertEquals(BufferedImage.TYPE_INT_ARGB, decoded.getType());
            int pixel = decoded.getRGB(50, 50);
            assertTrue((pixel >> 24) != 0, "Alpha channel should be preserved");
        }
    }

    @Test
    void oversizedImageDownscaledTo1200Width() throws IOException {
        byte[] jpegBytes = createTestImage(2000, 1500, "jpg");
        MockMultipartFile file = new MockMultipartFile(
                "image", "large.jpg", "image/jpeg", jpegBytes);

        ProcessedImage result = processor.process(file);

        assertNotNull(result);
        assertEquals(1200, result.width());
        assertEquals(900, result.height());
    }

    @Test
    void smallImageNotUpscaled() throws IOException {
        byte[] jpegBytes = createTestImage(400, 300, "jpg");
        MockMultipartFile file = new MockMultipartFile(
                "image", "small.jpg", "image/jpeg", jpegBytes);

        ProcessedImage result = processor.process(file);

        assertNotNull(result);
        assertEquals(400, result.width());
        assertEquals(300, result.height());
    }

    @Test
    void jpegExifOrientationIsNormalized() throws IOException {
        byte[] jpegBytes = createTestImage(40, 20, "jpg");
        MockMultipartFile file = new MockMultipartFile(
                "image", "oriented.jpg", "image/jpeg", withExifOrientation(jpegBytes, 6));

        ProcessedImage result = processor.process(file);

        assertEquals(20, result.width());
        assertEquals(40, result.height());
    }

    @Test
    void rejectsUnsupportedMimeType() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.gif", "image/gif", new byte[]{1});

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> processor.process(file)
        );
        assertTrue(exception.getMessage().contains("Format d'image non supporté"));
    }

    @Test
    void rejectsNullContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", null, new byte[]{1});

        assertThrows(IllegalArgumentException.class,
                () -> processor.process(file));
    }

    @Test
    void rejectsOversizedFile() {
        byte[] bigBytes = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "image", "big.jpg", "image/jpeg", bigBytes);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> processor.process(file)
        );
        assertTrue(exception.getMessage().contains("trop volumineux"));
    }

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "empty.jpg", "image/jpeg", new byte[0]);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> processor.process(file)
        );
        assertTrue(exception.getMessage().contains("vide"));
    }

    @Test
    void rejectsPixelBomb() throws IOException {
        byte[] jpegBytes = createTestImage(5000, 6000, "jpg");
        MockMultipartFile file = new MockMultipartFile(
                "image", "bomb.jpg", "image/jpeg", jpegBytes);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> processor.process(file)
        );
        assertTrue(exception.getMessage().contains("mégapixels"));
    }

    @Test
    void rejectsCorruptImage() {
        byte[] corruptBytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        MockMultipartFile file = new MockMultipartFile(
                "image", "corrupt.jpg", "image/jpeg", corruptBytes);

        assertThrows(Exception.class,
                () -> processor.process(file));
    }

    private byte[] createTestImage(int width, int height, String format) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, width, height);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    private byte[] withExifOrientation(byte[] jpeg, int orientation) throws IOException {
        byte[] exifSegment = new byte[]{
                (byte) 0xFF, (byte) 0xE1, 0x00, 0x22,
                'E', 'x', 'i', 'f', 0x00, 0x00,
                'M', 'M', 0x00, 0x2A, 0x00, 0x00, 0x00, 0x08,
                0x00, 0x01,
                0x01, 0x12, 0x00, 0x03, 0x00, 0x00, 0x00, 0x01,
                0x00, (byte) orientation, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00
        };

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(jpeg, 0, 2);
        output.write(exifSegment);
        output.write(jpeg, 2, jpeg.length - 2);
        return output.toByteArray();
    }
}
