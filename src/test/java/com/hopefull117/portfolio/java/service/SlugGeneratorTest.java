package com.hopefull117.portfolio.java.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlugGeneratorTest {

    private final SlugGenerator slugGenerator = new SlugGenerator();

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            Spring Boot Security|spring-boot-security
            Développer l'été en Français|developper-l-ete-en-francais
            L'article d'aujourd'hui|l-article-d-aujourd-hui
            Java, Spring: retour !|java-spring-retour
            Java & Spring Boot|java-spring-boot
              Plusieurs   espaces  |plusieurs-espaces
            ---Java___Spring///Boot---|java-spring-boot
            C++|c
            C#|c
            .NET|net
            GPT-5|gpt-5
            AI/ML|ai-ml
            """)
    void generatesExpectedSlug(String title, String expected) {
        assertEquals(expected, slugGenerator.generate(title));
    }

    @Test
    void rejectsNullTitle() {
        assertThrows(IllegalArgumentException.class, () -> slugGenerator.generate(null));
    }

    @Test
    void rejectsBlankTitle() {
        assertThrows(IllegalArgumentException.class, () -> slugGenerator.generate("   \t"));
    }

    @Test
    void rejectsPunctuationOnlyTitle() {
        assertThrows(IllegalArgumentException.class, () -> slugGenerator.generate("... !!! +++"));
    }

    @Test
    void rejectsEmojiOnlyTitle() {
        assertThrows(IllegalArgumentException.class, () -> slugGenerator.generate("🚀✨"));
    }

    @Test
    void boundsLongSlugsAndKeepsCanonicalFormat() {
        String slug = slugGenerator.generate("word ".repeat(40));

        assertTrue(slug.length() <= SlugGenerator.MAX_LENGTH);
        assertTrue(slug.matches("[a-z0-9]+(?:-[a-z0-9]+)*"));
    }

    @Test
    void generationDoesNotDependOnDefaultLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("i-java", slugGenerator.generate("I JAVA"));
        } finally {
            Locale.setDefault(original);
        }
    }
}
