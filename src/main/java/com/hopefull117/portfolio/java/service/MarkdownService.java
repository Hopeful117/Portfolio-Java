package com.hopefull117.portfolio.java.service;

import com.hopefull117.portfolio.java.dto.RenderedMarkdown;
import com.hopefull117.portfolio.java.dto.TableOfContentsEntry;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.HtmlWriter;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MarkdownService {
    private static final List<Extension> EXTENSIONS = List.of(TablesExtension.create());
    private static final Pattern CALLOUT_MARKER = Pattern.compile("^\\[!(NOTE|FINDING|WARNING)]\\s*");

    private final Parser parser;

    public MarkdownService() {
        this.parser = Parser.builder().extensions(EXTENSIONS).build();
    }

    public RenderedMarkdown render(String markdown) {
        Node document = parser.parse(markdown == null ? "" : markdown);
        List<Heading> headings = collectHeadings(document);
        Map<Heading, String> headingIds = createHeadingIds(headings);
        List<TableOfContentsEntry> tableOfContents = headings.stream()
                .filter(heading -> heading.getLevel() == 2 || heading.getLevel() == 3)
                .map(heading -> new TableOfContentsEntry(
                        headingIds.get(heading), plainText(heading), heading.getLevel()))
                .toList();

        transformCallouts(document);

        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(EXTENSIONS)
                .escapeHtml(true)
                .sanitizeUrls(true)
                .nodeRendererFactory(new MarkdownNodeRendererFactory(headingIds))
                .build();
        return new RenderedMarkdown(renderer.render(document), tableOfContents);
    }

    public String toHtml(String markdown) {
        return render(markdown).html();
    }

    private List<Heading> collectHeadings(Node document) {
        List<Heading> headings = new ArrayList<>();
        document.accept(new AbstractVisitor() {
            @Override
            public void visit(Heading heading) {
                headings.add(heading);
                visitChildren(heading);
            }
        });
        return headings;
    }

    private Map<Heading, String> createHeadingIds(List<Heading> headings) {
        Map<Heading, String> headingIds = new IdentityHashMap<>();
        Map<String, Integer> occurrences = new HashMap<>();
        for (Heading heading : headings) {
            String baseId = headingId(plainText(heading));
            int occurrence = occurrences.merge(baseId, 1, Integer::sum);
            headingIds.put(heading, occurrence == 1 ? baseId : baseId + "-" + occurrence);
        }
        return headingIds;
    }

    private String headingId(String label) {
        String normalized = Normalizer.normalize(label, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return normalized.isBlank() ? "section" : normalized;
    }

    private String plainText(Node node) {
        StringBuilder text = new StringBuilder();
        appendPlainText(node.getFirstChild(), text);
        return text.toString().trim();
    }

    private void appendPlainText(Node node, StringBuilder text) {
        for (Node current = node; current != null; current = current.getNext()) {
            if (current instanceof Text textNode) {
                text.append(textNode.getLiteral());
            } else if (current instanceof Code code) {
                text.append(code.getLiteral());
            } else if (current instanceof Image image) {
                appendPlainText(image.getFirstChild(), text);
            } else {
                appendPlainText(current.getFirstChild(), text);
            }
        }
    }

    private void transformCallouts(Node document) {
        for (Node current = document.getFirstChild(); current != null; ) {
            Node next = current.getNext();
            transformCallouts(current);
            if (current instanceof BlockQuote blockQuote) {
                CalloutBlock callout = calloutFrom(blockQuote);
                if (callout != null) {
                    blockQuote.insertBefore(callout);
                    moveChildren(blockQuote, callout);
                    blockQuote.unlink();
                }
            }
            current = next;
        }
    }

    private void moveChildren(Node source, Node target) {
        for (Node child = source.getFirstChild(); child != null; ) {
            Node next = child.getNext();
            child.unlink();
            target.appendChild(child);
            child = next;
        }
    }

    private CalloutBlock calloutFrom(BlockQuote blockQuote) {
        if (!(blockQuote.getFirstChild() instanceof Paragraph paragraph)
                || !(paragraph.getFirstChild() instanceof Text markerText)) {
            return null;
        }
        Matcher matcher = CALLOUT_MARKER.matcher(markerText.getLiteral());
        if (!matcher.find()) {
            return null;
        }
        String remaining = markerText.getLiteral().substring(matcher.end());
        if (remaining.isBlank()) {
            markerText.unlink();
        } else {
            markerText.setLiteral(remaining);
        }
        return new CalloutBlock(matcher.group(1));
    }

    private static class MarkdownNodeRendererFactory implements HtmlNodeRendererFactory {
        private final Map<Heading, String> headingIds;

        private MarkdownNodeRendererFactory(Map<Heading, String> headingIds) {
            this.headingIds = headingIds;
        }

        @Override
        public NodeRenderer create(HtmlNodeRendererContext context) {
            return new MarkdownNodeRenderer(context, headingIds);
        }
    }

    private static class MarkdownNodeRenderer implements NodeRenderer {
        private final HtmlNodeRendererContext context;
        private final Map<Heading, String> headingIds;
        private final HtmlWriter html;

        private MarkdownNodeRenderer(HtmlNodeRendererContext context, Map<Heading, String> headingIds) {
            this.context = context;
            this.headingIds = headingIds;
            this.html = context.getWriter();
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Set.of(Heading.class, Paragraph.class, CalloutBlock.class);
        }

        @Override
        public void render(Node node) {
            if (node instanceof Heading heading) {
                renderHeading(heading);
            } else if (node instanceof Paragraph paragraph && isFigure(paragraph)) {
                renderFigure((Image) paragraph.getFirstChild());
            } else if (node instanceof Paragraph paragraph) {
                renderParagraph(paragraph);
            } else if (node instanceof CalloutBlock callout) {
                renderCallout(callout);
            } else {
                context.render(node);
            }
        }

        private void renderHeading(Heading heading) {
            html.line();
            html.tag("h" + heading.getLevel(), Map.of("id", headingIds.get(heading)));
            renderChildren(heading);
            html.tag("/h" + heading.getLevel());
            html.line();
        }

        private boolean isFigure(Paragraph paragraph) {
            return paragraph.getFirstChild() instanceof Image
                    && paragraph.getFirstChild().getNext() == null;
        }

        private void renderFigure(Image image) {
            html.line();
            html.tag("figure");
            context.render(image);
            if (image.getTitle() != null && !image.getTitle().isBlank()) {
                html.tag("figcaption");
                html.text(image.getTitle());
                html.tag("/figcaption");
            }
            html.tag("/figure");
            html.line();
        }

        private void renderParagraph(Paragraph paragraph) {
            html.line();
            html.tag("p");
            renderChildren(paragraph);
            html.tag("/p");
            html.line();
        }

        private void renderCallout(CalloutBlock callout) {
            html.line();
            html.tag("aside", Map.of("class", "article-callout article-callout-"
                            + callout.type.toLowerCase(Locale.ROOT),
                    "aria-label", callout.type));
            html.tag("strong", Map.of("class", "article-callout-label"));
            html.text(callout.type);
            html.tag("/strong");
            renderChildren(callout);
            html.tag("/aside");
            html.line();
        }

        private void renderChildren(Node parent) {
            for (Node child = parent.getFirstChild(); child != null; child = child.getNext()) {
                context.render(child);
            }
        }
    }

    private static class CalloutBlock extends CustomBlock {
        private final String type;

        private CalloutBlock(String type) {
            this.type = type;
        }
    }
}
