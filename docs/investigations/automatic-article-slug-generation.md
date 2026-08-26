# Automatic Article Slug Generation - Investigation

Status: Complete

Evidence labels used throughout this report:

- **OBSERVED**: directly verified in repository history, source, configuration, or tests.
- **INFERRED**: conclusion supported by observed evidence but not directly encoded.
- **PROPOSED**: recommended future behavior; not implemented.
- **NOT VERIFIED**: unavailable or not established during this investigation.

# Executive Summary

- **OBSERVED**: Portfolio stores articles as MongoDB documents. MongoDB `_id` is the stable internal/admin identity; `slug` is a separate mutable string and the sole public lookup identity at `/blog/{slug}` (`Article.java:12-26`, `ArticleController.java:17,38-49`).
- **OBSERVED**: Both create and edit forms require direct manual slug entry. The controller binds forms directly to `Article`, and `ArticleService` saves the submitted slug without generation, normalization, validation, collision handling, or history (`form-articles.html:54-65`, `form-edit-articles.html:63-73`, `ArticleService.java:45-54,60-85`).
- **OBSERVED**: No repository-visible unique index protects slugs. Duplicate documents can therefore be written unless an untracked operational index exists; a single-result `findBySlug` nevertheless assumes uniqueness (`Article.java:12-47`, `ArticleRepository.java:10-17`).
- **OBSERVED**: Editing the slug immediately changes the public URL. No redirect, alias, canonical URL, sitemap, or feed preserves or advertises the move.
- **PROPOSED**: Generate a normalized slug on create, preserve it on every edit, remove manual slug inputs, resolve collisions with `-2`, `-3`, and so on, and enforce final uniqueness with a MongoDB unique index.
- **PROPOSED**: `ArticleService` should own create-versus-edit semantics and call a small deterministic `SlugGenerator`. This is `SIMPLE_UTILITY_SUFFICIENT`; introducing a `Slug` value object would create disproportionate binding and persistence changes.
- **PROPOSED**: Preserve every existing persisted slug. No bulk migration and no redirect history are required because the new policy prevents automatic changes to existing URLs.
- **NOT VERIFIED**: Live MongoDB documents and indexes could not be inspected because no Portfolio containers were running. Existing duplicate, blank, null, or unusual slugs must be audited before enabling a unique index.

# Current Article Lifecycle

## Create

1. **OBSERVED**: `GET /admin/articles/add` creates a blank `Article` and renders `admin/articles/form-articles` (`AdminController.java:241-251`).
2. **OBSERVED**: Thymeleaf binds title, slug, excerpt, Markdown content, cover image, tags, and `published` directly to the Mongo persistence model. There is no article form DTO (`form-articles.html:30-146`).
3. **OBSERVED**: `POST /admin/articles` receives `@Valid Article` and a multipart `image`, then delegates to `ArticleService.create` (`AdminController.java:254-263`).
4. **OBSERVED**: The service optionally stores the image, sets creation/update timestamps, and saves the document with `MongoRepository.save` (`ArticleService.java:45-54`).
5. **OBSERVED**: The controller also sets `createdAt`, but the service immediately replaces it (`AdminController.java:259`, `ArticleService.java:49-52`).

## Edit

1. **OBSERVED**: `GET /admin/articles/edit/{id}` retrieves the Mongo document by `_id` and binds it directly to the edit form (`AdminController.java:267-280`).
2. **OBSERVED**: `POST /admin/articles/edit/{id}` binds another `Article`; the path ID selects the existing document (`AdminController.java:283-294`).
3. **OBSERVED**: `ArticleService.update` copies title, slug, excerpt, content, tags, and publication state into the existing document, preserves `createdAt`, updates `updatedAt`, and saves (`ArticleService.java:60-85`).

## Read and publication

1. **OBSERVED**: `/blog` lists `published=true` articles ordered by descending creation time (`ArticleController.java:24-31`, `ArticleService.java:108-110`, `ArticleRepository.java:17`).
2. **OBSERVED**: `/blog/{slug}` performs an exact `findBySlug`, converts stored Markdown to HTML, and renders `public/article` (`ArticleController.java:38-49`, `ArticleService.java:116-133`).
3. **OBSERVED**: Detail lookup does not require `published=true`. Drafts are unlisted but reachable by anyone who knows the slug.
4. **INFERRED**: This draft exposure is a pre-existing publication defect, not a reason to make draft slugs mutable. It is outside the candidate slug Story.

Lifecycle summary:

`Thymeleaf form -> AdminController -> ArticleService -> Article Mongo document -> ArticleRepository -> MongoDB -> ArticleController -> MarkdownService -> public Thymeleaf route`

# Current Slug Semantics

- **OBSERVED**: Slug is the public URL identity and exact lookup key.
- **OBSERVED**: Slug is not the database primary key; MongoDB supplies a separate string `_id`.
- **OBSERVED**: Slug is manually entered on create and edit.
- **OBSERVED**: Slug is mutable because update copies the submitted value.
- **OBSERVED**: Slug is not required by HTML, Bean Validation, domain code, or repository code.
- **OBSERVED**: Slug uniqueness is assumed by `Optional<Article> findBySlug` but is not enforced in repository-visible application or Mongo configuration.
- **OBSERVED**: Slug is not an article filename. Markdown is stored in the Mongo `content` field, not in files or front matter.
- **OBSERVED**: Slug is used by the public detail route and blog-card link. Admin edit/delete routes use `_id`.
- **OBSERVED**: No slug use was found in SQL relationships, project pages, sitemap, feed, canonical metadata, or redirects.
- **NOT VERIFIED**: Persisted Markdown may contain literal `/blog/{slug}` internal links; live article content was unavailable.

# Current Persistence

- **OBSERVED**: `Article` is `@Document(collection = "articles")` and has a Spring Data Mongo string `@Id` (`Article.java:12-20`).
- **OBSERVED**: `slug` is an unconstrained `String` field (`Article.java:23-27`).
- **OBSERVED**: `ArticleRepository` extends `MongoRepository<Article, String>` (`ArticleRepository.java:10`).
- **OBSERVED**: Mongo uses database `portfolio`, with the URI supplied by `MONGO_URI` (`application.properties:15-16`).
- **OBSERVED**: Docker Compose persists Mongo data in `mongo_data`; no article schema migration, seed, or index initializer is tracked (`docker-compose.yml:31-40,71-74`).
- **OBSERVED**: PostgreSQL data and relationships are unrelated to articles.
- **NOT VERIFIED**: Live documents and Mongo indexes. `docker compose ps` reported no running services during the investigation.

# Current Routing

- **OBSERVED**: Public routes are `/blog` and `/blog/{slug}` (`ArticleController.java:17,24,38`).
- **OBSERVED**: Blog cards build the detail link from the current stored slug (`public/blog.html:114-121`).
- **OBSERVED**: Admin edit/delete routes identify articles by Mongo `_id` (`AdminController.java:267-306`).
- **OBSERVED**: No route resolves an old slug, no slug history exists, and no redirect is issued.
- **INFERRED**: Changing a slug leaves the document/admin workflow intact but breaks its former public URL, external links, bookmarks, indexed search results, social shares, and literal Markdown links.
- **INFERRED**: A missing old slug likely becomes an unhandled server error because `EntityNotFoundException` has no response status or exception handler. Runtime status was not exercised.

# Current Form UX

- **OBSERVED**: Create and edit each display an ordinary editable `Slug` text input.
- **OBSERVED**: Neither form shows a generated URL preview, validation feedback, `required`, `pattern`, or `maxlength` behavior.
- **OBSERVED**: Forms bind directly to the persistence entity rather than a dedicated command/form object.
- **INFERRED**: The user must understand URL-safe syntax and manually keep title/slug choices consistent because the application provides no policy.

# Existing Validation

- **OBSERVED**: Controller methods use `@Valid`, but `Article` declares no Jakarta validation constraints (`AdminController.java:254-255,283-286`, `Article.java:18-47`).
- **OBSERVED**: Neither controller action accepts `BindingResult`; forms do not render field or global errors.
- **OBSERVED**: No slug lowercasing, trimming, allowed-character check, non-empty check, or maximum length exists.
- **INFERRED**: Blank, null, whitespace, mixed-case, accented, punctuation-heavy, and slash-containing values can reach persistence.

# Existing Uniqueness Guarantees

- **OBSERVED**: No `@Indexed(unique = true)`, explicit Mongo index creation, `existsBySlug`, duplicate lookup, or duplicate-key handling exists.
- **OBSERVED**: `findBySlug` returns `Optional<Article>`, which models at most one result without enforcing that invariant.
- **INFERRED**: In the repository-defined state, duplicate slugs can be saved and later make lookup ambiguous or fail with a non-unique-result data access error.
- **NOT VERIFIED**: A manually created production index may exist outside source control.
- **PROPOSED**: Use application collision selection for friendly suffixes and a database unique index as the final concurrency-safe guarantee. A pre-save `exists` check alone is insufficient.

# Existing Article Compatibility

- **OBSERVED**: Existing articles already carry persisted slugs and are loaded independently by Mongo `_id` in admin workflows.
- **PROPOSED**: Do not recalculate, normalize, or migrate any existing slug.
- **PROPOSED**: Remove slug from edit submission and preserve the stored value in `ArticleService.update`, including unusual legacy values.
- **PROPOSED**: Audit existing data for missing/blank/duplicate slugs before enabling a unique index. Corrective data work, if actually needed, requires a separately approved plan and is not silently included.
- **NOT VERIFIED**: Whether current production data contains duplicate, null, blank, non-ASCII, or internally linked slugs.

# URL Stability Analysis

| Option | Assessment |
|---|---|
| A. Generate on create only; keep stable | **PROPOSED**. Safest and smallest. It protects all links without redirect infrastructure. |
| B. Regenerate whenever title changes | Rejected. It breaks public URLs and would require slug history plus permanent redirects. |
| C. Regenerate only while draft | Rejected. The current draft state is only a boolean and draft details are already publicly reachable; it also adds lifecycle-dependent identity rules. |
| D. Manual explicit regeneration | Rejected for this Story. No demonstrated requirement justifies retaining dangerous manual URL mutation. |
| E. Stable ID in URL | Rejected as a larger routing and SEO redesign inconsistent with the requested small change. |

- **PROPOSED**: Select Option A. Slug becomes an immutable public identifier after article creation, regardless of publication status.

# Create vs Edit Semantics

- **PROPOSED CREATE**: Server generates a slug from the submitted title before first persistence.
- **PROPOSED EDIT**: Title may change; persisted slug remains unchanged.
- **PROPOSED DRAFT/PUBLISHED**: Publication transitions never regenerate a slug.
- **PROPOSED LEGACY**: Existing persisted slug wins, even if it does not satisfy the new generator's canonical format.

# Slug Normalization Rules

**PROPOSED deterministic policy:**

1. Require a nonblank title.
2. Normalize Unicode to decomposed form and remove combining diacritic marks.
3. Lowercase with a locale-independent locale.
4. Treat every run of characters outside ASCII `a-z` and `0-9` as one `-`. This includes whitespace, apostrophes, punctuation, ampersands, underscores, slashes, and symbols.
5. Collapse repeated separators and strip leading/trailing `-`.
6. Reject an empty result with a meaningful title/slug validation error.
7. Limit the base slug to 100 characters, strip any trailing separator after truncation, and reserve room for a collision suffix. Cutting at the last separator when practical is preferable but need not become a general text framework.
8. Keep the output contract `[a-z0-9]+(?:-[a-z0-9]+)*`.

This policy deliberately prefers predictable ASCII URLs over universal transliteration.

# Technical Title Cases

Under the proposed generic policy:

| Title | Base slug |
|---|---|
| `Vers un developpement pilote par l'IA` (with French accents in input) | `vers-un-developpement-pilote-par-l-ia` |
| `Java & Spring Boot : retour d'experience` (with French accents in input) | `java-spring-boot-retour-d-experience` |
| `C++ / C# et .NET` | `c-c-et-net` |
| `OpenClaw, OpenCode & DevLog AI` | `openclaw-opencode-devlog-ai` |
| `  Plusieurs   espaces  ` | `plusieurs-espaces` |
| `Spring Boot` | `spring-boot` |
| `GPT-5` | `gpt-5` |
| `AI/ML` | `ai-ml` |

- **PROPOSED**: `C++` becomes `c`, `C#` becomes `c`, and `.NET` becomes `net`. Do not add a growing dictionary such as `plus-plus`, `sharp`, or `dotnet` in the first Story.
- **INFERRED**: Some technical titles normalize to the same base; the uniqueness policy handles collisions without making normalization language- or technology-specific.

# Duplicate Slug Strategy

| Strategy | Assessment |
|---|---|
| Reject and ask for another title | Predictable but poor UX after removing manual slug control; changing a valid title solely for a URL is undesirable. |
| Append `-2`, `-3`, ... | **PROPOSED**. Readable, deterministic relative to persisted state, SEO-friendly, and small. |
| Append year/date | Can still collide and couples identity to time without a product requirement. |
| Append database ID | Concurrency-safe after persistence but less readable and requires a two-step identity/update design. |

- **PROPOSED**: First article uses `spring-boot-security`; subsequent collisions use `spring-boot-security-2`, then `-3`, choosing the first available suffix.
- **PROPOSED**: Uniqueness applies across all articles, including drafts, because `/blog/{slug}` is one global namespace.
- **PROPOSED**: A Mongo unique index is authoritative. Application checks choose a friendly candidate; duplicate-key handling retries with the next suffix for bounded attempts and otherwise returns a meaningful form-level conflict.
- **PROPOSED**: Index rollout must follow an explicit live-data audit. Do not depend solely on `existsBySlug` under concurrency.

# Server vs Client Responsibility

- **PROPOSED**: Use `SERVER-SIDE ONLY` in the candidate Story.
- **PROPOSED**: The server generates, validates, resolves collisions, and persists the slug.
- **PROPOSED**: Do not duplicate normalization in browser JavaScript. Client-side-only generation is not authoritative and client preview plus server duplication creates drift risk.

# Manual Slug Field Decision

- **PROPOSED: REMOVE** the editable slug input from both create and edit forms.
- **PROPOSED**: Do not retain an advanced override or legacy edit escape hatch without a demonstrated use case.
- **PROPOSED**: On edit, URL stability is enforced by not binding or copying any submitted slug.

# Preview UX Assessment

- **PROPOSED: NICE_TO_HAVE**, not required.
- A live preview would duplicate the generator in JavaScript or require a server preview endpoint, expanding scope and drift risk.
- The smallest safe Story may show no preview. A later server-rendered persisted URL on edit could be considered separately if author feedback shows a need.

# SEO / Sitemap / Feed Impact

- **OBSERVED**: Article pages set an HTML title but expose no canonical URL, meta description, OpenGraph/Twitter URL metadata, or structured article data (`public/article.html:5`, `fragments/head.html:1-20`).
- **OBSERVED**: No sitemap, RSS/Atom feed, or robots configuration exists in the repository.
- **OBSERVED**: Blog cards are the only repository-visible generated article-detail links.
- **INFERRED**: Immutable create-time slugs improve link stability without requiring changes to currently nonexistent sitemap/feed/canonical features.
- **PROPOSED**: Do not add SEO infrastructure to the slug Story. Future SEO work can consume the stable slug contract.

# Redirect Assessment

- **PROPOSED: NO** redirects are required for this change because existing slugs remain untouched and future edits do not regenerate slugs.
- **PROPOSED**: If explicit slug changes are introduced later, old-slug history and permanent redirects become required before enabling that behavior.

# Architectural Ownership

## Option A - Controller generation

- Low initial effort, but mixes URL identity rules into the web adapter.
- Duplicates behavior across entry points and cannot safely own persistence collision retries.
- **PROPOSED**: Reject.

## Option B - Inline application service generation

- `ArticleService` already owns create/update persistence and can distinguish create from edit.
- Embedding normalization directly would mix pure string policy with orchestration.
- **PROPOSED**: Accept service orchestration, but extract normalization.

## Option C - Dedicated deterministic component used by the application layer

- Pure, cohesive, reusable, and directly unit-testable.
- `ArticleService` calls it only during create and owns uniqueness/retry behavior.
- `ArticleRepository` supports collision lookup; MongoDB owns the hard unique invariant.
- Matches existing delegation to `MarkdownService` without creating a framework.
- **PROPOSED**: Recommend.

## Option D - Entity callback/listener

- Would obscure create-versus-edit semantics and risk silently regenerating existing slugs on saves.
- **PROPOSED**: Reject.

# Slug Value Object Assessment

**SIMPLE_UTILITY_SUFFICIENT**

- **OBSERVED**: Slug has meaningful invariants and equality, but the current application directly binds a mutable Mongo entity, queries by `String`, and renders a string in Thymeleaf.
- **INFERRED**: A value object would require MVC conversion, Mongo conversion/storage decisions, repository signature changes, DTO/template adaptation, and legacy compatibility handling. It still would not enforce cross-document uniqueness.
- **PROPOSED**: Keep `Article.slug` as `String`; centralize new-slug construction in one deterministic component and enforce invariants at creation/service/database boundaries.

# Failure UX

- **PROPOSED EMPTY TITLE**: Re-render create form with a title-required message.
- **PROPOSED EMPTY GENERATED SLUG**: Re-render with a message that the title must contain letters or numbers.
- **PROPOSED DUPLICATE BASE**: Automatically choose the next numeric suffix; no user action required.
- **PROPOSED PERSISTENCE RACE**: Catch the domain-relevant duplicate-key failure, retry bounded suffix selection, and never expose a Mongo/SQL exception.
- **PROPOSED UNEXPECTED CONFLICT**: Preserve entered form values and show a global save error.
- **PROPOSED LEGACY SLUG**: Preserve and continue routing it verbatim; do not reject an existing article merely because its slug is noncanonical.
- **PROPOSED VALIDATION OWNERSHIP**: Use Bean Validation for title input, generator/domain-policy validation for canonical output, application logic for collision UX, and a Mongo unique index for the final invariant.

# Existing Test Coverage

- **OBSERVED**: The only test is `JavaApplicationTests.contextLoads()` (`src/test/java/com/hopefull117/portfolio/java/JavaApplicationTests.java:6-11`).
- **OBSERVED**: No article controller, service, repository, form, route, slug, duplicate, publication, Markdown, URL, template, or failure tests exist.
- **OBSERVED**: Article/slug behavior originated mainly in commit `c0a89b2` (`refonte style front-end + refactoring`); Markdown rendering was added in `8771c8a` (`ecriture des articles en markdown`). Later article commits are presentation-only.

# Proposed Test Matrix

## Slug normalization unit tests

- Basic ASCII title and lowercase output.
- French accents/diacritics and apostrophes.
- Punctuation, ampersands, underscores, and slashes.
- Repeated whitespace/separators and leading/trailing separators.
- `C++`, `C#`, `.NET`, `GPT-5`, and `AI/ML` outputs.
- Blank, punctuation-only, emoji-only, and null title rejection.
- Maximum length, suffix room, deterministic output, and locale independence.

## Create/service tests

- Slug is generated and persisted from title.
- Submitted/manual slug data cannot override generation.
- First collision uses `-2`; later collision chooses the first available suffix.
- Duplicate-key race retries and terminal failure maps to an application error.
- Timestamps, cover image, tags, Markdown content, and publication behavior remain intact.

## Edit/service tests

- Changing title preserves existing slug.
- Publishing/unpublishing preserves slug.
- Legacy unusual slug remains unchanged.
- Existing image and `createdAt` preservation remain intact.

## Repository/integration tests

- Mongo unique index rejects duplicate slugs.
- Generated article is reachable at `/blog/{generated-slug}`.
- Existing article remains reachable at its stored legacy slug.

## MVC/form tests

- Create and edit forms no longer contain editable slug inputs.
- Empty/invalid title re-renders with meaningful errors and preserved values.
- Successful create/edit redirects remain unchanged.

# DevLog MCP Benchmark

## Frozen pre-repository benchmark

- **OBSERVED**: Before Portfolio source inspection, the available MCP resource and resource-template registries were queried. Both were empty, and no DevLog MCP tools were exposed in this session.
- **NOT VERIFIED**: `get_engineering_context`, freshness/checkpoints, and `search_project_history` could not be called.
- **OBSERVED**: DevLog supplied no article workflow, related Story/history, persistence convention, route information, decision, or source-file evidence.
- **OBSERVED**: All substantive findings in this report required direct repository and Git-history fallback.
- **OBSERVED**: DevLog did not reduce exploration effort in this session. Generic local Git evidence is not credited to DevLog.

## Assessment

- `CONTEXT_USEFULNESS: NONE`
- `REPOSITORY_FALLBACK: HIGH`

This result measures MCP availability in the investigation session, not the quality of an accessible DevLog knowledge base. Because the required tools were unavailable, no honest post-Story-0095 content-quality assessment can be made. The benchmark should be repeated only in a future session where the Portfolio DevLog MCP project and tools are actually exposed; repository findings from this investigation must not be used to inflate that replay.

# Architecture Options

| Option | Cohesion | Testability | Reuse | Complexity | Decision |
|---|---|---|---|---|---|
| A. Controller | Low | Medium | Low | Low initially | Reject |
| B. Inline `ArticleService` | Medium | Medium | Low | Lowest | Partial: orchestration only |
| C. Small generator + `ArticleService` | High | High | High enough | Small | **Recommend** |
| D. Entity callback/listener | Low/hidden | Medium | Medium | Medium | Reject |

# Recommended Behavior Table

| Operation | Title change | Slug behavior |
|---|---|---|
| Create | New title | Generate normalized base slug server-side |
| Create collision | Same normalized base | Use first available `-2`, `-3`, ... suffix |
| Edit | No | Preserve persisted slug |
| Edit | Yes | Preserve persisted slug |
| Publish/unpublish | Any | Preserve persisted slug |
| Existing legacy article | Any | Preserve persisted slug verbatim |
| Concurrent create | Same candidate | Mongo unique index decides; application retries next suffix |
| Invalid/empty generated result | New title | Reject create with meaningful form error |

# Recommended Implementation

**PROPOSED** future implementation sequence, all within one small Story:

1. Add one deterministic slug generator with the normalization contract above.
2. Generate the slug only in `ArticleService.create`.
3. Stop copying/binding slug in edit so updates preserve the stored value.
4. Remove editable slug inputs from create and edit forms.
5. Add repository collision support, numeric suffix selection, bounded duplicate-key retry, and a database unique index after a live-data preflight.
6. Add focused generator, service, repository/integration, MVC/form, routing, and legacy-preservation tests.
7. Return meaningful validation/conflict errors without exposing persistence exceptions.

# Scope / Non-Scope

## Scope

- Deterministic ASCII slug generation.
- Create-only service integration.
- Immutable edit semantics.
- Numeric suffix collisions and database uniqueness.
- Manual slug field removal.
- Validation and focused regression tests.
- Existing slug compatibility.

## Non-Scope

- Slug history or redirects.
- Existing slug normalization or bulk migration.
- Sitemap, feed, canonical, OpenGraph, or broader SEO redesign.
- JavaScript/live URL preview.
- Publication workflow or draft-access repair.
- Markdown rendering/security changes.
- Article form DTO/domain refactor.
- Universal language or technology transliteration.

# Candidate Engineering Story

**Generate Stable Unique Article Slugs on Creation**

No Engineering Story was created by this investigation.

# Risks

- **NOT VERIFIED**: Live Mongo data may contain duplicate, null, or blank slugs that prevent unique-index creation.
- **NOT VERIFIED**: A production-only Mongo index may already exist and must be reconciled with source-controlled ownership.
- **OBSERVED**: No article tests currently protect behavior, so the future Story must establish its own regression baseline.
- **INFERRED**: Numeric suffix allocation can race unless duplicate-key recovery is designed around the unique index.
- **INFERRED**: Direct entity binding and absent `BindingResult` handling make good validation UX a little broader than string generation alone.
- **OBSERVED**: `git fetch` failed because the SSH key agent refused the hardware-backed key. Local `main` and cached `origin/main` were both `5f9d2be`, but current remote freshness is not verified.
- **OBSERVED**: Draft detail access by slug is not publication-protected; this is pre-existing and explicitly outside slug scope.

# Open Questions

- **NOT VERIFIED**: Does production Mongo contain duplicate, missing, blank, or unusual slugs?
- **NOT VERIFIED**: Does production Mongo already have an operational unique slug index?
- **NOT VERIFIED**: Do persisted Markdown bodies or external systems contain links to current article slugs?
- **NOT VERIFIED**: Is there any untracked operational proxy/CDN behavior for canonical hosts, HTTPS, redirects, or error mapping?
- **PROPOSED**: These questions do not change the recommended create-only stability model. The first two must be answered as a deployment preflight before enabling the database unique index.
