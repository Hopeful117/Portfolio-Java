# Portfolio Shared Long-Form Reading Primitives

Status: Complete

## 1. Problem

The Portfolio stored article bodies as Markdown and rendered them with the base CommonMark parser and HTML renderer. That was sufficient for ordinary prose, but it did not provide the shared primitives needed by richer technical articles:

- stable heading IDs;
- server-derived table of contents data;
- Markdown tables;
- bounded overflow for tables and code;
- semantic figures and captions;
- semantic technical callouts.

This implementation improves the shared Article rendering pipeline only. It does not introduce an Article profile or a Deep Dive content type.

## 2. Previous Rendering Pipeline

```text
Article.content
    |
    v
ArticleService.findBySlug
    |
    v
MarkdownService.toHtml
    |
    v
CommonMark Parser + HtmlRenderer
    |
    v
ArticleViewDto.content: String
    |
    v
public/article.html
    |
    v
th:utext
```

The persistence model was not changed. `Article.content` remains the authored Markdown string stored in MongoDB.

## 3. Resulting Rendering Pipeline

```text
Article.content
    |
    v
ArticleService.findBySlug
    |
    v
MarkdownService.render
    |
    +--> CommonMark Parser with GFM table extension
    |
    +--> AST heading analysis and deterministic ID allocation
    |
    +--> AST callout transformation for supported blockquote markers
    |
    v
HtmlRenderer with controlled custom node renderers
    |
    v
RenderedMarkdown
  html
  tableOfContents
    |
    v
ArticleViewDto
  content
  tableOfContents
    |
    +--> Thymeleaf server-generated TOC
    |
    +--> th:utext rendered article body
```

`RenderedMarkdown` is an immutable record. `TableOfContentsEntry` is also an immutable record containing `id`, `label`, and `level`.

The Markdown AST is parsed once per article rendering. Heading analysis, callout transformation, and HTML rendering all operate within the same rendering operation. TOC IDs are taken from the same identity-based heading map used by the heading renderer.

## 4. Article Model and Routes

```text
Article persistence/domain model changed: NO
Deep Dive profile introduced: NO
MongoDB migration required: NO
Public routes changed: NO
```

The existing model fields remain authoritative. The existing `/blog` and `/blog/{slug}` routes remain unchanged. No `STANDARD`, `DEEP_DIVE`, `EngineeringDeepDive`, new collection, new lifecycle, or DevLog integration was added.

## 5. Heading IDs

All Markdown headings receive an `id` attribute. H2 and H3 headings are eligible for the TOC; H1 and H4-H6 still receive IDs for direct linking.

### Normalization algorithm

1. Extract visible heading text from the AST.
2. Preserve text from ordinary text nodes and inline code nodes.
3. Normalize Unicode using NFD.
4. Remove combining marks, which makes common accented Latin text stable in ASCII form.
5. Lowercase using `Locale.ROOT`.
6. Replace every run outside ASCII letters and digits with `-`.
7. Remove leading and trailing `-`.
8. Use `section` if the result is empty.
9. Allocate `-2`, `-3`, and so on for repeated base IDs in document order.

Examples:

```text
Experimental Design       -> experimental-design
Data & Metrics             -> data-metrics
Café `results`             -> cafe-results
Experimental Design        -> experimental-design-2
```

This intentionally does not attempt full international transliteration. It is deterministic, human-readable, and suitable for the existing Portfolio content. Unchanged heading text in unchanged order produces the same ID.

## 6. Table of Contents

The TOC is derived from the same AST and heading-ID map used for HTML rendering. Each entry has:

```text
TableOfContentsEntry
  id
  label
  level
```

### Heading levels

The initial TOC includes H2 and H3 only. The article title is rendered separately by the Thymeleaf page as H1. H4-H6 remain linkable through generated IDs but are not included in the initial TOC.

### Display rule

The server always derives eligible entries. The Thymeleaf TOC is rendered only when there are more than one eligible entries. A document with no eligible headings or one eligible heading therefore does not receive a redundant navigation block.

### Presentation

The template uses semantic `<nav aria-label="Table des matières">`, an ordered list, and keyboard-usable ordinary anchor links. The TOC is inline within the article body and is not sticky, floating, animated, or scroll-spy driven.

## 7. Markdown Extensions

Added:

```text
org.commonmark:commonmark-ext-gfm-tables:0.24.0
```

The existing CommonMark engine remains in place at version `0.24.0`. The same `TablesExtension` instance is registered with both the parser and HTML renderer. No Markdown engine replacement or syntax-highlighting dependency was added.

The figure and callout behavior uses custom node rendering in the existing CommonMark pipeline rather than arbitrary raw HTML authored by article authors.

## 8. Tables

GFM-style Markdown tables now render as semantic `<table>`, `<thead>`, `<tbody>`, `<tr>`, `<th>`, and `<td>` markup. Existing alignment markers are preserved by the CommonMark table extension.

Example:

```markdown
| Metric | Direct |
|---|---:|
| Correct | 6 |
```

The article CSS gives tables borders, spacing, readable header treatment, and a bounded mobile overflow container. On narrow screens, the table scrolls horizontally inside the article content rather than forcing destructive cell wrapping or page-level overflow.

## 9. Code Blocks

The renderer does not add syntax highlighting. Fenced and indented code remain CommonMark output.

The article CSS now gives `pre` blocks:

- `max-width: 100%`;
- horizontal overflow when required;
- preserved whitespace;
- a bounded dark code surface.

Inline code is kept distinct and is not forced into block-style overflow behavior. Long source lines therefore remain readable without breaking the article card layout.

## 10. Figure Convention

The minimum supported figure convention is a standalone Markdown image paragraph:

```markdown
![Benchmark result](charts/result.png "Benchmark result by condition")
```

The renderer emits:

```html
<figure>
  <img ... alt="Benchmark result">
  <figcaption>Benchmark result by condition</figcaption>
</figure>
```

An image without a Markdown title still receives semantic `<figure>` markup without a caption. The alt text remains authored in the normal Markdown image syntax. Figures are responsive through `max-width: 100%` and automatic height.

This convention is technology-neutral and works for static charts, diagrams, screenshots, and other authored image assets. It does not introduce figure numbering, cross-references, an asset database, or a chart runtime.

## 11. Callout Convention

The initial callout taxonomy is intentionally small:

```text
NOTE
FINDING
WARNING
```

Authors use a blockquote marker:

```markdown
> [!FINDING]
> The result is reproducible across the frozen repository revision.
```

The renderer recognizes only those exact supported marker types and emits controlled semantic markup:

```html
<aside class="article-callout article-callout-finding" aria-label="FINDING">
  <strong class="article-callout-label">FINDING</strong>
  <p>The result is reproducible across the frozen repository revision.</p>
</aside>
```

Unsupported markers remain ordinary blockquotes. The marker is not a general HTML escape mechanism and authors do not insert arbitrary classes or HTML.

## 12. Raw HTML Boundary

No new feature requires arbitrary authored raw HTML. Tables use the official CommonMark extension. Figures use standard Markdown image syntax. Callouts use a controlled blockquote convention transformed by the renderer.

The existing `th:utext` and unsanitized-rendering concern remains out of scope. This implementation does not broaden it deliberately. A future sanitization/security redesign remains a separate decision.

## 13. Accessibility

- TOC uses semantic `<nav>` with an accessible label.
- TOC entries are normal keyboard-usable anchor links.
- Tables remain semantic HTML tables.
- Figures use `<figure>` and optional `<figcaption>`.
- Image alt text remains authored through Markdown image syntax.
- Callouts use semantic `<aside>` and an accessible `aria-label`, not color alone.
- Mobile table and code overflow is contained within the article body.

## 14. Backward Compatibility

Existing persisted Markdown is not rewritten. The Article model and database documents are unchanged. Existing headings retain their visible text and hierarchy; heading IDs are additive. Existing paragraphs, lists, links, images, quotes, inline code, and code blocks continue to render.

Existing articles do not need author changes. The new TOC is hidden for documents with fewer than two eligible headings. Existing public URLs and listing/publication behavior are unchanged.

## 15. Tests Added and Updated

Added `MarkdownServiceTest` covering:

- existing headings, paragraphs, lists, links, images, quotes, inline code, and fenced code;
- H1-H4 heading IDs;
- punctuation and accents in heading IDs;
- duplicate heading collision suffixes;
- H2/H3 TOC labels, levels, and IDs;
- single-heading TOC data behavior;
- Markdown table semantic output and alignment;
- image figures and optional captions;
- supported FINDING callouts;
- unsupported callout markers remaining ordinary blockquotes;
- controlled callout output without arbitrary unknown callout classes.

Existing article service/controller suites were preserved. `ArticleService` now transports the derived rendering result into `ArticleViewDto`, while article persistence and route behavior remain unchanged.

## 16. Test Results

Command:

```text
./mvnw test
```

Result:

```text
Tests run: 77, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The build also compiled the new CommonMark table dependency and ran the Spring context test with the existing Testcontainers setup.

## 17. Files Changed

- `pom.xml`
- `src/main/java/com/hopefull117/portfolio/java/dto/ArticleViewDto.java`
- `src/main/java/com/hopefull117/portfolio/java/dto/RenderedMarkdown.java`
- `src/main/java/com/hopefull117/portfolio/java/dto/TableOfContentsEntry.java`
- `src/main/java/com/hopefull117/portfolio/java/service/MarkdownService.java`
- `src/main/java/com/hopefull117/portfolio/java/service/ArticleService.java`
- `src/main/resources/templates/public/article.html`
- `src/main/resources/static/css/public/article.css`
- `src/test/java/com/hopefull117/portfolio/java/service/MarkdownServiceTest.java`
- `docs/implementation/portfolio-long-form-reading-primitives.md`

No Article model, MongoDB document, route, admin form, publication lifecycle, or existing article content file was changed.

## 18. Deferred Findings

The following findings remain intentionally deferred:

- draft articles reachable by known slug;
- general Markdown/HTML sanitization redesign;
- SEO metadata and canonical URLs;
- OpenGraph metadata;
- RSS and sitemap;
- article search;
- reading progress and sticky navigation;
- syntax highlighting;
- Deep Dive profile or content type;
- research, dataset, notebook, and attachment metadata.

## 19. Limitations and Unresolved Questions

- The TOC is intentionally flat with level-based indentation rather than nested ordered lists.
- H4-H6 IDs exist but are excluded from the initial TOC.
- Figures must be standalone image paragraphs; mixed image/text paragraphs remain ordinary Markdown paragraphs.
- Figure captions use the standard Markdown image title. There is no figure numbering or cross-reference syntax.
- Callouts recognize only `NOTE`, `FINDING`, and `WARNING`, and the marker must begin the first text node of a blockquote.
- Existing raw HTML behavior is unchanged and still requires a future security decision.
- No browser automation or visual device testing was available in this implementation; CSS behavior is covered by scoped rules and should receive visual review before release.
- The future Deep Dive profile may choose which of these shared primitives to emphasize, but it has not been introduced.

## 20. Deferred Deep Dive Work

The following remain for a later, separately approved Story:

```text
Article.profile = STANDARD | DEEP_DIVE
Deep Dive-specific presentation policy
research/reproducibility metadata
appendix-specific navigation
related ADR/Story presentation
dataset/notebook artifact policy
future DevLog generation targeting
```

This Story delivers the shared rendering foundation only.

## 21. Mandatory Answers A-T

### A

```text
NO
```

The `Article` persistence/domain model was not changed.

### B

```text
NO
```

No Deep Dive profile was introduced.

### C

```text
YES
```

Stable heading IDs were implemented for all rendered Markdown headings.

### D

```text
YES
```

Duplicate heading IDs use deterministic document-order suffixes such as `results-2`.

### E

```text
YES
```

The TOC is generated server-side from the CommonMark AST.

### F

```text
YES
```

TOC IDs come from the same heading-ID map used by HTML rendering.

### G

```text
H2 and H3
```

H1 and H4-H6 receive IDs but are not included in the initial TOC.

### H

```text
YES
```

GFM Markdown table support was implemented using the official CommonMark Java extension.

### I

```text
YES
```

Wide tables scroll within a bounded article table container on narrow screens.

### J

```text
YES
```

Long code blocks use intentional horizontal overflow without destructive wrapping.

### K

```text
YES
```

Standalone Markdown images now render as semantic figures with optional title-based captions.

### L

```text
NOTE, FINDING, WARNING
```

### M

```text
NO
```

No new feature requires arbitrary authored raw HTML.

### N

```text
NO
```

The CommonMark engine was not replaced.

### O

```text
NO
```

Existing public routes were unchanged.

### P

```text
NO
```

No MongoDB migration was required.

### Q

```text
YES
```

Existing Markdown rendering behavior is covered by focused regression tests.

### R

```text
NO
```

Unrelated draft, security, SEO, feed, sitemap, and search findings were not modified.

### S

```text
YES
```

`./mvnw test` passed with 77 tests, zero failures, and zero errors.

### T

```text
YES
```

The shared rendering foundation is ready for a later Deep Dive profile Story, subject to separate architectural and editorial approval.

## 22. Recommended Next Portfolio Step

Human review the rendering conventions and the public article UX, especially:

1. whether image titles are the preferred caption authoring convention;
2. whether the three initial callout types are sufficient;
3. whether the inline TOC is visually appropriate on mobile;
4. whether the current raw HTML trust boundary requires a separate security Story before broader content authoring.

Do not start the Deep Dive profile Story until these shared primitives and their content conventions are accepted.

<COMPLETION_CLASSIFICATION>
LONG_FORM_READING_PRIMITIVES_COMPLETE
STOP_FOR_HUMAN_REVIEW
