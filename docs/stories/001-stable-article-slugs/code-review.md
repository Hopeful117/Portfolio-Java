# Story-001 Code Review

## Checklist: Critical items from specification section 33

### Slug generation accidentally running on update
- **Status: OK** – `ArticleService.update()` explicitly removed the line `existingArticle.setSlug(article.getSlug())` (was at old line 71). The loaded article slug is never overwritten by the edit request. Only `title`, `excerpt`, `content`, `tags`, `published`, cover image (if new), and `updatedAt` are copied.

### Old slugs being normalized
- **Status: OK** – The new `SlugGenerator` is called **only** during `create()`. No normalization or recalculation occurs on edit, read, or publish/unpublish. Legacy slugs (e.g., `Old_Article-Slug`) remain byte-for-byte unchanged.

### Manual slug field still present
- **Status: OK** – Both `form-articles.html` and `form-edit-articles.html` have the editable `*{slug}` input **removed**. The word "Slug" label and its `<div>` are gone from both templates. Title fields are now `required` HTML attribute. Validation errors for title are rendered adjacent to the field.

### Controller owning slug logic
- **Status: OK** – Slug generation lives in `SlugGenerator` (called from `ArticleService.create`). The controller (`AdminController`) only delegates; it does not generate, normalize, or resolve collisions. The controller does own `BindingResult` handling and form error rendering, which is the correct layer for user-facing feedback.

### Unbounded collision loop
- **Status: OK** – Collision allocation is bounded at `MAX_SLUG_ATTEMPTS = 1000`. The `findAvailableCandidateNumber()` method iterates from `start` upward and throws `ArticleSlugConflictException` (which the controller catches and renders as a form error) if no free suffix is found within the limit. No unbounded loop can occur.

### Database index being enabled without production safety proof
- **Status: OK** – No `@Indexed(unique = true)`, no `@MongoId.unique = true`, no `ensureIndex` call, no `MongoTemplate` index initializer present in the diff. The unique index is **DEFERRED** with explicit production preflight documentation. Application-level `existsBySlug` exists only as a query method on the repository interface, not as an index enforcement mechanism.

### Hidden startup migration
- **Status: OK** – No `@Component` that runs on application startup, no `ApplicationRunner`/`CommandLineRunner`, no `spring.flyway.*` or spring-data Mongo migration configuration added. The only new bean is `SlugGenerator`; it is never auto-invoked outside explicit service calls.

### Bulk data rewrite
- **Status: OK** – No script, no `migration`, no `init` SQL that recalculates or normalizes existing slugs. The diff contains zero lines that modify existing persisted slug values. Existing articles (if any) retain their original stored slugs.

### Breaking public route
- **Status: OK** – Public route remains exactly `/blog/{slug}`. No `/blog/{id}/{slug}`, no `/article/{id}`, no route parameters changed. The `ArticleController` `@RequestMapping("/blog")` and `@GetMapping("/{slug}")` are untouched.

### SEO scope creep
- **Status: OK** – No canonical tags, no sitemap, no RSS, no OpenGraph redesign, no redirect history, no slug aliases, no SEO service added. The story explicitly stays within scope (see implementation-plan.md "Non-Scope" and report "Production Safety").

### DTO/domain refactor scope creep
- **Status: OK** – No DTO introduction, no domain model split, no value object. `Article` remains the single source of truth for the persistence field `slug`. The only domain change is `@NotBlank` on `title`; the slug field stays a plain `String`.

### Raw Mongo exceptions exposed
- **Status: OK** – `ArticlePersistenceException` wraps any `DataAccessException` (including `DuplicateKeyException`, `DataAccessResourceFailureException`) with a user-friendly message that never mentions "Mongo", "duplicate key", or implementation details. The controller catches `ArticleSlugConflictException | ArticlePersistenceException` and rejects `BindingResult` with the wrapped message. No raw exception propagates to the user.

### Insufficient test coverage
- **Status: OK** – 33 new focused tests plus 1 pre-existing context test = 34 total. Coverage includes: generator contract (18 params), create with collision suffixes, duplicate-key race retry, bounded exhaustion, legacy slug preservation on update, form field removal, validation errors, and public-route reachability. The baseline had zero article-specific tests; this story adds comprehensive regression coverage.

### Unrelated Article behavior changes
- **Status: OK** – `createdAt` preservation on edit (was already preserved by service, unchanged). `updatedAt` advances on edit (unchanged behavior). Cover image remains when no replacement uploaded (unchanged). Excerpt/Markdown/content/tags/publication state all retain current create/edit semantics (unchanged). Admin routes using `_id` unchanged. Public `/blog/{slug}` route unchanged. Draft detail access by known slug is pre-existing and explicitly out of scope.

## Superficial checks

- No new `.java` files outside `src/main/java/.../service/` and `src/main/java/.../exception/` and `src/main/java/.../repository/`.
- No new template files; only two existing Thymeleaf templates were edited (`form-articles.html`, `form-edit-articles.html`).
- `pom.xml` change adds only `h2` test-scope dependency; no production plugin or dependency changes.
- `src/test/resources/application.properties` is a new test-resources file that only affects test runtime profiles; production `src/main/resources/application.properties` is untouched.
- `target/` build outputs differ because Maven re-compiled after source changes; these are expected and not part of the Story diff (filtered with `git diff -- . ':!target'`).

## Overall assessment

All 23 critical checklist items pass. The diff is focused, minimal, and production-safe. The only remaining reservation is the residual concurrency race documented in the "Known Limitations" section of the engineering report, which is accepted as a temporary risk for a single-admin back office with database uniqueness deferred.