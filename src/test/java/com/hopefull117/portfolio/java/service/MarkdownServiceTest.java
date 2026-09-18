package com.hopefull117.portfolio.java.service;

import com.hopefull117.portfolio.java.dto.RenderedMarkdown;
import com.hopefull117.portfolio.java.dto.TableOfContentsEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownServiceTest {
    private final MarkdownService markdownService = new MarkdownService();

    @Test
    void rendersExistingMarkdownFeatures() {
        RenderedMarkdown rendered = markdownService.render("""
                # Title

                Paragraph with **strong** text.

                - one
                - two

                [link](https://example.com)

                ![Diagram](diagram.png)

                > Quote

                `inline`

                ```java
                System.out.println("hello");
                ```
                """);

        assertTrue(rendered.html().contains("<h1 id=\"title\">Title</h1>"));
        assertTrue(rendered.html().contains("<strong>strong</strong>"));
        assertTrue(rendered.html().contains("<ul>"));
        assertTrue(rendered.html().contains("href=\"https://example.com\""));
        assertTrue(rendered.html().contains("<figure>"));
        assertTrue(rendered.html().contains("<blockquote>"));
        assertTrue(rendered.html().contains("<code>inline</code>"));
        assertTrue(rendered.html().contains("<pre><code class=\"language-java\">"));
    }

    @Test
    void createsStableCollisionSafeHeadingIdsAndH2H3Toc() {
        RenderedMarkdown rendered = markdownService.render("""
                # Article title
                ## Experimental Design
                ### Data & Metrics
                ## Experimental Design
                #### Hidden detail
                ## Café `results`
                """);

        assertTrue(rendered.html().contains("<h2 id=\"experimental-design\">Experimental Design</h2>"));
        assertTrue(rendered.html().contains("<h3 id=\"data-metrics\">Data &amp; Metrics</h3>"));
        assertTrue(rendered.html().contains("<h2 id=\"experimental-design-2\">Experimental Design</h2>"));
        assertTrue(rendered.html().contains("<h4 id=\"hidden-detail\">Hidden detail</h4>"));
        assertTrue(rendered.html().contains("<h2 id=\"cafe-results\">Café <code>results</code></h2>"));
        assertEquals(List.of(
                        new TableOfContentsEntry("experimental-design", "Experimental Design", 2),
                        new TableOfContentsEntry("data-metrics", "Data & Metrics", 3),
                        new TableOfContentsEntry("experimental-design-2", "Experimental Design", 2),
                        new TableOfContentsEntry("cafe-results", "Café results", 2)),
                rendered.tableOfContents());
    }

    @Test
    void keepsOneHeadingAvailableButTheTemplateCanHideTheSmallToc() {
        assertEquals(1, markdownService.render("## Only section").tableOfContents().size());
    }

    @Test
    void rendersMarkdownTables() {
        String html = markdownService.toHtml("""
                | Metric | Direct |
                |---|---:|
                | Correct | 6 |
                """);

        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("<thead>"));
        assertTrue(html.contains("<th>Metric</th>"));
        assertTrue(html.contains("<td align=\"right\">6</td>"));
    }

    @Test
    void rendersFiguresWithOptionalCaptionFromImageTitle() {
        String html = markdownService.toHtml("![Chart](chart.png \"Benchmark result\")");

        assertTrue(html.contains("<figure>"));
        assertTrue(html.contains("alt=\"Chart\""));
        assertTrue(html.contains("<figcaption>Benchmark result</figcaption>"));
    }

    @Test
    void rendersOnlySupportedCalloutsAsControlledAsideMarkup() {
        String html = markdownService.toHtml("""
                > [!FINDING]
                > The result is reproducible.

                > [!UNKNOWN]
                > This remains an ordinary quote.
                """);

        assertTrue(html.contains("class=\"article-callout article-callout-finding\"")
                && html.contains("aria-label=\"FINDING\"")
                && html.contains("The result is reproducible."));
        assertTrue(html.contains("<blockquote>\n<p>[!UNKNOWN]"));
        assertFalse(html.contains("article-callout-unknown"));
    }

    @Test
    void escapesRawHtmlAndSanitizesUnsafeUrls() {
        String html = markdownService.toHtml("""
                <script>alert(1)</script>

                [unsafe](javascript:alert(2))

                ![unsafe](javascript:alert(3))

                [safe](https://example.com)
                """);

        assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
        assertFalse(html.contains("<script>"));
        assertFalse(html.contains("javascript:alert"));
        assertTrue(html.contains("href=\"https://example.com\""));
    }
}
