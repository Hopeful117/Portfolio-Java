# Story-001 Implementation Report

## Overview

Implemented stable automatic article slug generation on creation while preserving existing public URLs and ensuring production compatibility. All 34 tests pass (33 new + 1 context test) against H2 in-memory SQL + test MongoDB.

## What Was Implemented

### 1. `SlugGenerator` (`src/main/java/.../service/SlugGenerator.java`)
- Pure deterministic component, Spring `@Component`, no framework dependencies
- NFD Unicode normalization + diacritic removal (`\\p{M}+` → "")
- Locale-independent lowercase via `Locale.ROOT`
- Runs of non-`[a-z0-9]` become single `-`; leading/trailing `-` stripped
- 100-character maximum with trailing separator cleanup
- Empty result rejection with meaningful `IllegalArgumentException`
- Output contract `[a-z0-9]+(?:-[a-z0-9]+)*`

### 2. Create semantics (`src/main/java/.../service/ArticleService.java`)
- Slug generated only in `create()`, ignoring any client-submitted slug value
- Base slug allocated via bounded `findAvailableCandidateNumber()` (max 1000 attempts)
- First-available suffix selection: base → `-2` → `-3` → ...
- Duplicate-key `DuplicateKeyException` retries with next candidate, bounded
- Terminal persistence failures wrapped in `ArticlePersistenceException` (never exposes Mongo details)
- Article ID explicitly nullified before save to prevent update/upsert semantics

### 3. Edit / URL stability (`src/main/java/.../service/ArticleService.java`)
- `update()` **does not** copy `article.getSlug()` into the loaded entity
- Every existing persisted slug (including legacy noncanonical values) remains verbatim
- Title/content/publication/image/cover updates all preserve the loaded slug

### 4. Manual slug field removal
- `form-articles.html`: editable slug `<input>` removed entirely; title now `required`
- `form-edit-articles.html`: editable slug `<input>` removed entirely; title now `required`
- Both templates render global and field-level validation errors (`th:if` + `th:errors`)
- Dashboard slug display preserved for administrator visibility

### 5. Controller validation (`src/main/java/.../controller/AdminController.java`)
- `createArticle()` accepts `BindingResult`; title errors and slug conflicts render the form
- `IllegalArgumentException` from generator rejected on title field
- `ArticleSlugConflictException` / `ArticlePersistenceException` rejected with global error
- `editArticle()` similarly guards with `BindingResult`; persistence failures produce form-level error
- Neither controller exposes Mongo/SQL exceptions to the user

### 6. Repository collision support (`src/main/java/.../repository/ArticleRepository.java`)
- Added `boolean existsBySlug(String slug)` method signature (no index declaration)

### 7. Title validation on domain entity (`src/main/java/.../model/Article.java`)
- Added `@NotBlank(message = "Le titre est obligatoire")` on `title` field
- Removed unused `UUID` import

### 8. Focused test suite (33 new tests, 1 pre-existing = 34 total)
- `SlugGeneratorTest` (18 cases): basic ASCII, French accents/apostrophes, punctuation/ampersands, repeated separators, leading/trailing separators, `C++`/`C#`/`.NET`/`GPT-5`/`AI-ML`, blank/emoji-only/null titles, length bound, locale independence
- `ArticleServiceTest` (8 cases): create generates/persists slug while preserving data; collisions use `-2`/`-3`; duplicate-key race retries with `-2`; bounded exhaustion throws `ArticleSlugConflictException`; cover image persisted once; update preserves legacy slug (`Old_Article-Slug`)/`createdAt`/cover while editing fields; cover update without slug change; persistence failure translated to application error
- `AdminArticleControllerTest` (6 cases): successful create still redirects; blank title returns form with friendly error; title without slug characters returns error; persistence failure returns form without exposing cause; successful edit still redirects; both forms contain no editable slug field
- `ArticleControllerTest` (2 cases): generated slug remains reachable at existing `/blog/{slug}` public route
- `JavaApplicationTests.contextLoads()` (1 case): passes with H2 + test Mongo setup

## What Was NOT Done (Safety Choices)

- **No Mongo unique index** declared, enabled, or auto-created in source
- **No bulk slug normalization** or migration of existing documents
- **No startup initializer** that rewrites data on deploy
- **No entity callback/listener** for slug generation
- **No JavaScript/client-side** slug generation
- **No redirect/alias/SEO infrastructure** (canonical tags, sitemap, RSS, OpenGraph)
- **No routing redesign** – `/blog/{slug}` shape unchanged
- **No Article DTO/domain refactor** – `Article` entity remains the MVC model
- **No distributed locking** or concurrency primitives beyond bounded retry

## Production Safety (Mandatory Section)

### Public URL compatibility
Existing persisted slugs are never regenerated, normalized, or copied from edit requests. `/blog/{slug}` remains unchanged.

### Data compatibility
No bulk migration, startup rewrite, corrective script, or legacy validation pass. New articles store slug as a normal string; legacy slugs are preserved verbatim.

### Database assumptions
Source control proves the application uses MongoDB with no managed slug index. Production Mongo documents and indexes are unknown and inaccessible in this session. Existing null, missing, blank, duplicate, unusual slugs, or an operational index are all possible.

### Unique index
**DEFERRED. Unique database enforcement deferred pending production-data preflight.** Enabling an index during ordinary deployment could fail against unknown legacy data. Application checks provide friendly allocation but retain a concurrent-create race. Bounded duplicate-key retries support an already-present operational unique index and the future managed index without making deployment depend on either.

### Rollback
Application rollback is data-compatible. Newly created canonical slugs are ordinary strings already supported by the old application and remain readable through `/blog/{slug}`. No irreversible transformation or schema change is introduced.

### Deployment risk
**LOW** after verification. The change is create-only for slug generation, updates explicitly retain loaded slugs, routing and storage type are unchanged, and no migration/index activation occurs. Residual risk is concurrent same-title creation before database uniqueness is established.

### Manual production preflight
No production check required for this application-only deployment because no index is activated. Before a future unique-index maintenance step, an authorized operator must run read-only checks in `mongosh` against the intended database (commands provided in implementation-plan.md). These queries do not mutate data and are not executed by this Story.

### Verification focus
- Generator contract and locale independence
- Automatic create slug and submitted-slug override prevention
- `-2`/`-3` first-available collision behavior and bounded exhaustion
- Stable slug through title/content/publication/image edits
- Exact preservation of `Old_Article-Slug` or equivalent fixture
- No editable slug controls
- Existing redirects and `/blog/{slug}` route
- No index, migration, callback, or bulk rewrite in the final diff

## Verification

All 34 tests pass (`mvn test`: `Tests run: 34, Failures: 0, Errors: 0, Skipped: 0`). The complete test suite exercises the slug generator, create semantics, update/editing stability, controller validation, template field removal, and public-route reachability against a generated slug.

## Branch State

- Branch: `story/stable-article-slugs` (created from local `main` at `5f9d2be`)
- No destructive changes to existing production data
- Ready for human review/PR merge

## Known Limitations

- Application-level collision checks have a race (two concurrent same-title creates may both pass the `existsBySlug` check before either persists). For a single-admin back office this is an acceptable temporary risk if DB uniqueness must remain deferred.
- Direct entity binding without a form command means minimal error handling is somewhat broader than a dedicated DTO would provide; the smallest coherent solution was implemented.
- No production Mongo data was accessible; the deferred unique index depends on a future preflight that an authorized operator must execute.
- Docker Compose `mvn test` environment uses H2 in-memory SQL and a test Mongo at `localhost:27017/portfolio-test`; the full production runtime with the VPS configuration is not exercised here.
- The existing `JavaApplicationTests.contextLoads()` now passes only because the test configuration supplies `spring.datasource.url=jdbc:h2:mem:portfolio` and `spring.mongodb.uri=mongodb://localhost:27017/portfolio-test`; the original `DB_URL` environment variable must start with `jdbc` for production compatibility.

## DevLog Effectiveness

- **MCP available:** YES (tools were exposed in this OpenCode session)
- **Freshness:** `NO_BASELINE` (reported at repository revision `5f9d2be`, status `PROJECT_CONTEXT_STALE`, guidance `ESTABLISH_BASELINE`)
- **Useful evidence:** Initial context identified general article/UI history; `search_project_history` returned no matches for article creation, slug/routing, or the automatic-slug investigation; one Mongo/publication search timed out; DevLog did not provide the completed investigation or create/update semantics
- **Missing evidence:** DevLog could not discover the investigation, this Story, relevant commits, URL stability reasoning, or implementation files — repository fallback was required
- **Repository fallback:** HIGH (all substantive findings required direct Git-history and source-code inspection rather than DevLog queries)

## Suggested Next Story

Implement a MongoDB unique index on `articles.slug` after a live-data preflight audit, including the read-only verification commands documented in the Production Safety section and the migration of application collision handling into a concurrency-safe database invariant.