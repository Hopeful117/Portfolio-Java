# Portfolio 2.0 Public Content Contract and Security Boundaries

Status: Complete

## 1. Story Objective

This Story establishes a read-oriented Spring Boot public content boundary for the future Angular application without replacing the current Thymeleaf experience.

Implemented:

- `/api/public/**` namespace;
- public Article summary and detail DTOs;
- published-only public Article lookup;
- published-only existing `/blog/{slug}` lookup;
- bounded public Project summary API;
- deterministic public error semantics;
- server-side Markdown trust policy;
- tests for publication filtering, DTO boundaries, Markdown safety, and HTTP contracts.

Not implemented:

- Angular;
- new public frontend routes;
- homepage, ecosystem, or activity APIs;
- project detail route/model;
- Deep Dive profile;
- DevLog/Developer OS integration;
- publishing proposals or autonomous publication;
- SEO redesign;
- database migration;
- admin security rewrite.

## 2. Repository State Inspected

The implementation was based on the current source, including the recent long-form rendering work:

- `Article`, `Project`, `Technology` models;
- `ArticleRepository`, `ProjectRepository`;
- `ArticleService`, `ProjectService`, `MarkdownService`, and `PublicProjectService`;
- `ArticleViewDto`, `RenderedMarkdown`, and `TableOfContentsEntry`;
- public and admin controllers;
- `SecurityConfig` and `WebConfig`;
- CommonMark 0.24 and GFM tables dependency;
- existing article, project, Markdown, controller, and application tests;
- current Thymeleaf templates and routes.

The repository remains a Spring MVC application with PostgreSQL/JPA for project/profile data and MongoDB for articles.

## 3. Public and Private Boundary

The public boundary is now explicit:

```text
Internal persistence/entity
        |
        | explicit mapping
        v
Public DTO
        |
        v
/api/public/**
```

Persistence entities, MongoDB IDs, JPA IDs, publication flags, admin fields, security objects, and relationship internals are not returned by the new API.

The absence of `published` from public Article DTOs is intentional. A returned public Article is already approved for public visibility.

## 4. API Namespace

Implemented namespace:

```text
/api/public/**
```

The security configuration explicitly permits this read-oriented namespace. It does not introduce a generic `/api/**` administrative surface or API version prefix.

API versioning was deliberately not introduced. The Portfolio and future Angular client are coordinated within the same repository/product boundary, and no compatibility requirement justifies `/api/v1` yet.

## 5. Endpoints Implemented

### Article listing

```text
GET /api/public/articles
```

Returns `ResponseEntity<List<PublicArticleSummaryDto>>` using the existing published, newest-first repository query.

### Article detail

```text
GET /api/public/articles/{slug}
```

Returns `ResponseEntity<PublicArticleDetailDto>` after a repository-level published-and-slug lookup.

### Project listing

```text
GET /api/public/projects
```

Returns `ResponseEntity<List<PublicProjectSummaryDto>>` mapped from current Project data and technology names/icons.

## 6. Endpoints Deferred

Not implemented because their product contracts are not yet approved:

```text
GET /api/public/home
GET /api/public/ecosystem
GET /api/public/activity
GET /api/public/projects/{slug}
POST /api/publishing/proposals
```

The current `Project` model has only a generated numeric database ID and no stable public slug. A project detail endpoint would therefore require a public identifier policy and a bounded Project Evidence contract. No speculative slug or runtime ID exposure was introduced.

## 7. DTOs Introduced

### `PublicArticleSummaryDto`

```text
title
slug
excerpt
coverImage
createdAt
updatedAt
```

### `PublicArticleDetailDto`

```text
slug
excerpt
coverImage
createdAt
updatedAt
renderedHtml
```

`tableOfContents` uses the existing immutable `TableOfContentsEntry` projection with `id`, `label`, and `level`.

### `PublicProjectSummaryDto`

```text
imageUrl
repositoryUrl
technologies[]
```

### `PublicTechnologyDto`

```text
name
iconUrl
```

All DTOs are immutable Java records and copy list values defensively.

## 8. Published-Only Invariant

The public Article invariant is enforced at the repository/application boundary:

```text
PUBLIC ARTICLE <=> published == true
```

The repository now provides:

```java
Optional<Article> findBySlugAndPublishedTrue(String slug)
```

Public Article detail methods use this query. A draft slug and an unknown slug both produce `EntityNotFoundException`, which the public API maps to the same generic 404 response.

The public listing continues to use:

```java
findByPublishedTrueOrderByCreatedAtDesc()
```

Drafts cannot appear in the public list.

## 9. Existing `/blog/{slug}` Behavior

### Previous behavior

The existing route called an unconstrained `findBySlug` lookup. A draft was hidden from `/blog` but reachable if its slug was known.

### Result

The existing route now calls `ArticleService.findPublicViewBySlug`, which uses the same published-only repository query as the new API.

```text
/blog/{published-slug} -> public article view
/blog/{draft-slug}    -> 404
/blog/{unknown-slug}  -> 404
```

`EntityNotFoundException` is now explicitly a 404 exception. The public Thymeleaf route and public API therefore do not silently disagree about what a public Article is.

### Admin behavior

Authenticated admin workflows continue to use ID-based `ArticleService.findById`. That operation does not apply the public publication filter and can still retrieve drafts for editing/deletion. The separation is intentional:

```text
findById                 -> internal/admin lookup
findPublicViewBySlug     -> published public Thymeleaf view
findPublicDetailBySlug   -> published public API detail
findPublicSummaries      -> published public API listing
```

## 10. Markdown Flow

The flow remains:

```text
Article.content
      |
      v
MarkdownService.render()
      |
      +--> CommonMark AST
      +--> stable heading IDs
      +--> H2/H3 TOC
      +--> GFM tables
      +--> figures
      +--> NOTE/FINDING/WARNING callouts
      |
      v
RenderedMarkdown
      |
      +--> existing ArticleViewDto for Thymeleaf
      +--> PublicArticleDetailDto for API
```

Markdown is not reparsed in controllers or delegated to Angular. The existing long-form primitives remain Spring-owned.

The public Article API exposes rendered HTML and derived TOC data. It does not expose raw Markdown.

## 11. HTML Trust Investigation

### Current risk

Before this Story, CommonMark rendered raw HTML and unsafe URL schemes through the existing Thymeleaf `th:utext` path. The repository had no visible server-side sanitizer.

Potential unsafe inputs included:

```markdown
<script>alert(1)</script>
[unsafe](javascript:alert(2))
![unsafe](javascript:alert(3))
```

This risk was relevant to both the existing public page and the future API.

### Authoring model

Current Article content is authored through the authenticated admin Article forms. Future AI-generated content is not trusted by this Story and must pass a separate validation workflow before publication.

### CommonMark capability audit

The existing CommonMark Java 0.24 renderer supports two bounded options relevant here:

- `escapeHtml(true)`;
- `sanitizeUrls(true)`.

CommonMark documentation also makes clear that URL sanitization is not a complete HTML sanitizer. The selected policy therefore avoids relying on it alone: authored raw HTML is escaped, and URL schemes are sanitized.

## 12. Selected HTML Trust Policy

The implemented policy is:

```text
Server-side render-time safety policy:

1. CommonMark raw HTML is escaped with HtmlRenderer.escapeHtml(true).
2. Link and image destinations are sanitized with HtmlRenderer.sanitizeUrls(true).
3. Controlled figure/callout markup is emitted only by the Markdown renderer's
   application-owned node renderers.
4. Public APIs expose only this rendered result.
5. Angular must not use bypassSecurityTrustHtml as the trust model.
```

Consequences:

- authored `<script>`, raw tags, and event-handler attributes are emitted as escaped text rather than executable markup;
- `javascript:` URLs are not emitted as executable destinations;
- ordinary safe HTTPS links remain functional;
- existing Markdown headings, links, code, tables, images, figures, and callouts remain supported;
- persisted Article content is not migrated or rewritten.

This is a bounded policy, not a general HTML sanitizer. Raw HTML authoring is intentionally not supported as executable article markup.

## 13. Security Implications

### Addressed

- Public Article listing is published-only.
- Public Article detail is published-only.
- Known draft slugs return generic 404 behavior.
- Draft metadata/content are not returned.
- Public DTOs omit persistence IDs and publication state.
- Raw authored HTML is escaped at render time.
- Unsafe link/image URL schemes are sanitized.
- Unexpected API failures return a generic Problem Detail without internal exception details.
- `/api/public/**` is explicitly public read access.
- No JWT, WebFlux, CORS expansion, write API, or publishing integration was introduced.

### Not addressed

- Destructive admin GET operations remain security debt.
- The admin trust model remains account-based and assumes authorized authors are allowed to author content.
- No rate limiting was introduced.
- No CSP or broader security-header redesign was introduced.
- The future publication validation workflow does not exist yet.
- Existing public API deployment topology remains unchanged.

## 14. Error Contract

The new API uses Spring `ProblemDetail` for deterministic errors.

### Not found

```http
404 Not Found
Content-Type: application/problem+json
```

Example shape:

```json
{
  "title": "Resource not found",
  "status": 404,
  "detail": "The requested public resource was not found."
}
```

The response does not reveal whether the requested slug was a draft or never existed.

### Unexpected failure

```http
500 Internal Server Error
Content-Type: application/problem+json
```

The detail is generic:

```text
The public request could not be completed.
```

Database details, class names, stack traces, IDs, and exception messages are not returned by the scoped public API advice.

## 15. Project Public Contract

The current Project model reliably supports a bounded summary projection:

```text
imagePath -> imageUrl
githubUrl -> repositoryUrl
technologies -> name/iconUrl only
```

Numeric JPA IDs and relationship internals are not exposed.

The project list is implemented through `PublicProjectService`, which maps current repository data to public records. It is deliberately not a project case-study model.

## 16. Project Detail Decision

```text
Project detail endpoint: NO
```

The current model has no stable public slug, no uniqueness policy for public identifiers, and no bounded case-study fields. A detail route would require speculative domain expansion or unsafe numeric ID exposure.

Deferred to:

```text
Project Evidence Experience Story
```

## 17. Identifier Policy

- Articles use their existing stable slug.
- Public Article lookup is exact slug lookup plus `published=true`.
- MongoDB Article IDs are never exposed.
- Projects currently have no public identifier in the API.
- JPA numeric Project IDs are not exposed.
- Technologies are nested public projections without IDs.
- No runtime slug generation or slug persistence migration was introduced for Projects.

## 18. Future SEO Ownership

SEO infrastructure is not implemented in this Story. Future authority should remain server-backed and derive from the public content contract:

```text
canonical path
social image
publishedAt
updatedAt
```

Angular should render metadata from a stable API/content contract rather than independently inventing SEO values. Canonical URLs, OpenGraph, structured data, sitemap, and robots policy remain a later Story.

## 19. Future Developer OS Boundary

Documented future flow:

```text
Developer OS / DevLog
        |
        v
publication proposal
        |
        v
explicit human validation
        |
        v
accepted publication
        |
        v
Portfolio publishing boundary
        |
        v
Article persistence
        |
        v
public DTO/API
```

No DevLog classes, databases, agent traces, MCP internals, webhooks, events, message brokers, or autonomous publication were introduced.

## 20. Deep Dive Compatibility

`Article` remains the publishing aggregate. No `DEEP_DIVE` profile or new publishing aggregate was added.

The detail DTO already carries the shared long-form contract:

```text
renderedHtml
```

An optional future profile can add presentation/metadata behavior later without requiring a new aggregate or replacing this API shape. New profile-specific fields should be added only when the first approved Deep Dive needs them.

## 21. Tests Added and Updated

### Markdown trust tests

`MarkdownServiceTest` now verifies:

- raw `<script>` markup is escaped;
- unsafe `javascript:` links/images are not emitted as executable URLs;
- safe HTTPS links remain functional;
- existing headings, links, code, tables, figures, and callouts remain supported.

### Publication filtering tests

`ArticleServiceTest` verifies:

- public detail uses `findBySlugAndPublishedTrue`;
- draft lookup raises `EntityNotFoundException`;
- public summaries use the published listing query;
- admin/internal ID lookup still returns a draft.

`ArticleControllerTest` verifies a known draft slug is now a 404 on the existing `/blog/{slug}` route.

### Public Article API tests

`PublicArticleControllerTest` verifies:

- summary JSON shape;
- omission of `id` and `published`;
- detail `renderedHtml` and TOC contract;
- no raw `content` field;
- generic 404 Problem Detail for missing/draft slugs;
- generic 500 Problem Detail without service error details.

### Public Project API tests

`PublicProjectControllerTest` verifies bounded JSON shape and absence of IDs.

`PublicProjectServiceTest` verifies project and technology mapping.

### Existing regression tests

Existing Article, admin, image, storage, slug, controller, and application context tests were retained and executed.

## 22. Test Results

Command:

```text
./mvnw test
```

Result:

```text
Tests run: 89, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 23. Files Modified

Application files:

- `src/main/java/com/hopefull117/portfolio/java/config/SecurityConfig.java`
- `src/main/java/com/hopefull117/portfolio/java/controller/ArticleController.java`
- `src/main/java/com/hopefull117/portfolio/java/controller/PublicArticleController.java`
- `src/main/java/com/hopefull117/portfolio/java/controller/PublicApiExceptionHandler.java`
- `src/main/java/com/hopefull117/portfolio/java/controller/PublicProjectController.java`
- `src/main/java/com/hopefull117/portfolio/java/dto/PublicArticleDetailDto.java`
- `src/main/java/com/hopefull117/portfolio/java/dto/PublicArticleSummaryDto.java`
- `src/main/java/com/hopefull117/portfolio/java/dto/PublicProjectSummaryDto.java`
- `src/main/java/com/hopefull117/portfolio/java/dto/PublicTechnologyDto.java`
- `src/main/java/com/hopefull117/portfolio/java/exception/EntityNotFoundException.java`
- `src/main/java/com/hopefull117/portfolio/java/repository/ArticleRepository.java`
- `src/main/java/com/hopefull117/portfolio/java/service/ArticleService.java`
- `src/main/java/com/hopefull117/portfolio/java/service/MarkdownService.java`
- `src/main/java/com/hopefull117/portfolio/java/service/PublicProjectService.java`

Tests:

- `src/test/java/com/hopefull117/portfolio/java/controller/ArticleControllerTest.java`
- `src/test/java/com/hopefull117/portfolio/java/controller/PublicArticleControllerTest.java`
- `src/test/java/com/hopefull117/portfolio/java/controller/PublicProjectControllerTest.java`
- `src/test/java/com/hopefull117/portfolio/java/service/ArticleServiceTest.java`
- `src/test/java/com/hopefull117/portfolio/java/service/MarkdownServiceTest.java`
- `src/test/java/com/hopefull117/portfolio/java/service/PublicProjectServiceTest.java`

Documentation:

- `docs/implementation/portfolio-2-public-content-contract.md`

No Angular workspace, frontend dependency, route, visual design, homepage, project model, database schema, or Developer OS integration was added.

## 24. Deferred Security Debt

- Destructive admin GET operations.
- Broad `.anyRequest().permitAll()` policy review.
- Full CSRF/admin form verification.
- Rate limiting and security headers.
- Production TLS/reverse-proxy verification.
- Draft privacy in any future non-public preview workflow.
- Publication proposal validation workflow.
- Public artifact authorization.
- Full SEO/canonical/social metadata.

## 25. Acceptance Criteria

```text
public API boundary exists                         PASS
public endpoints expose DTOs only                  PASS
public article listing is published-only            PASS
public article detail is published-only             PASS
draft slug does not leak content                    PASS
existing admin draft access remains functional     PASS
existing public site remains operational            PASS
Markdown remains Spring-owned                       PASS
heading IDs and TOC remain Spring-owned             PASS
HTML trust policy is explicit                       PASS
unsafe rendered HTML is not executable              PASS
public article contract preserves primitives         PASS
project API is bounded                              PASS
no speculative project case-study model             PASS
no JWT                                             PASS
no Angular                                         PASS
no Developer OS integration                         PASS
tests pass                                         PASS
```

## 26. Mandatory Answers A-Z

### A

```text
YES
```

`/api/public/**` was introduced.

### B

```text
NO
```

Persistence entities are not exposed directly.

### C

```text
YES
```

Published Articles are retrievable by slug.

### D

```text
NO
```

Unpublished Articles are not retrievable through the new API by slug.

### E

```text
404
```

### F

```text
NO
```

### G

```text
YES
```

Admin ID-based lookup remains draft-capable.

### H

```text
YES
```

The existing `/blog/{slug}` route previously exposed known drafts.

### I

```text
YES
```

It now uses the published-only lookup and returns 404 for draft slugs.

### J

```text
SPRING
```

### K

```text
SPRING
```

### L

```text
SPRING
```

### M

```text
NO
```

Raw Markdown is not exposed by the Article API.

### N

```text
YES
```

The detail DTO exposes `renderedHtml`.

### O

```text
Raw HTML is escaped server-side and URL destinations are sanitized server-side before rendered HTML is exposed. Controlled figure/callout markup is renderer-owned. Arbitrary authored raw HTML is not executable public HTML.
```

### P

```text
NO
```

Under the selected policy, raw authored HTML cannot become executable public HTML through the Markdown output.

### Q

```text
NO
```

### R

```text
NO
```

### S

```text
NO
```

Spring MVC was not replaced by WebFlux.

### T

```text
NO
```

Project detail was deferred because no stable public project slug or case-study contract exists.

### U

```text
NO
```

### V

```text
NO
```

### W

```text
NO
```

### X

```text
NO
```

### Y

```text
NO
```

### Z

```text
YES
```

All 89 tests pass.

## 27. Additional Security Answers AA-AH

### AA

```text
YES
```

Public lookups are publication-filtered at repository/application boundaries.

### AB

```text
NO
```

Draft and missing slugs share a generic 404 Problem Detail.

### AC

```text
YES
```

The Markdown/HTML trust boundary is documented.

### AD

```text
YES
```

Raw HTML, unsafe URLs, safe links, and public API error leakage are tested.

### AE

```text
YES
```

The repository contains no committed/live article corpus available for direct production audit; representative published-content fixtures covering existing Markdown primitives and hostile inputs were checked. Live production Mongo content remains NOT VERIFIED.

### AF

```text
YES
```

Public responses use deliberately selected DTO fields.

### AG

```text
NO
```

Destructive admin GET operations were not redesigned.

### AH

```text
NO
```

This Story does not authorize autonomous publication.

## 28. Classification

```text
PORTFOLIO_2_PUBLIC_CONTENT_CONTRACT_READY
```

## 29. Exact Next Human Decision

Authorize or reject the public contract and trust-policy implementation. If authorized, the next bounded Story is:

```text
Story 2 — Angular Application Foundation & Hybrid Rendering
```

Do not begin that Story automatically. It should first receive human authorization after reviewing:

- the `/api/public/**` DTO shapes;
- published-only behavior on both API and existing Thymeleaf article routes;
- the server-side rendered-HTML trust policy;
- the deferred project-detail boundary;
- the explicit exclusion of Angular, DevLog integration, and autonomous publication.

PORTFOLIO_2_PUBLIC_CONTENT_CONTRACT_READY
READY_FOR_HUMAN_AUTHORIZATION_ANGULAR_APPLICATION_FOUNDATION
STOP_FOR_HUMAN_REVIEW
