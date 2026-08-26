# Story-001 Repository Analysis

## Revision And Git State

- Local `main`, the starting investigation branch, and cached `origin/main` were all `5f9d2be1e89baa856726175372171d654cebb92c` before branching.
- `git fetch` failed because the SSH agent refused the hardware-backed key. Current remote freshness is therefore not verified.
- Story branch: `story/stable-article-slugs`, created from local `main`.
- The completed investigation was present as an untracked artifact and was read in full before implementation.

## Investigation Revalidation

The current code agrees with the investigation, with two wording qualifications: slug inputs are editable but not technically required, and the multipart cover image is a separate controller argument rather than an `Article` property.

### Create

`form-articles.html` binds an `Article` directly and exposes title and slug. `AdminController.createArticle` receives that entity and delegates to `ArticleService.create`. The service stores an optional image, sets both timestamps, and calls `ArticleRepository.save` without slug generation or collision handling.

### Edit

`form-edit-articles.html` exposes an editable slug. `AdminController.editArticle` binds a new `Article`; `ArticleService.update` loads the persisted document by Mongo `_id` and currently copies the submitted slug along with editable content.

### Public Read

`ArticleController` keeps the public route at `GET /blog/{slug}`. `ArticleService.findBySlug` performs an exact repository lookup. Mongo `_id` is internal/admin identity while slug is public URL identity. No redirect or slug-history mechanism exists.

### Persistence And Indexes

`Article` is a Mongo document with an unconstrained string slug. `ArticleRepository` has `findBySlug` but no collision query. No source-controlled Mongo index initializer, migration mechanism, or automatic index creation is present. Production documents and indexes were not inspected.

### Validation And Errors

`@Valid` is present but `Article` has no constraints, controller methods have no `BindingResult`, and article forms render no validation errors. Mongo persistence errors are not translated to form feedback.

### Existing Behavior To Preserve

- Cover image remains unchanged on edit when no replacement is supplied.
- `createdAt` remains unchanged on edit and `updatedAt` advances.
- Excerpt, Markdown content, tags, and publication state retain current create/edit behavior.
- Admin routes continue to identify articles by Mongo `_id`.
- The public route shape remains `/blog/{slug}`.
- Draft detail access by known slug is a pre-existing defect and remains out of scope.

## Test Baseline

- Existing suite: one `JavaApplicationTests.contextLoads()` test and no article-specific tests.
- `./mvnw test`: could not start because `.mvn/wrapper/maven-wrapper.properties` is missing.
- `mvn test`: `1` test run, `0` failures, `1` error. The context failed before Story behavior was exercised because `DB_URL` did not start with `jdbc`.
- `docker compose ps --all`: no local containers running.

## DevLog Evidence

DevLog MCP was available. It reported repository revision `5f9d2be` with freshness `NO_BASELINE` and warning `PROJECT_CONTEXT_STALE`. Searches for article creation, slug/routing, and the automatic-slug investigation returned no matches; one Mongo/publication search timed out. Initial context identified general article/UI history but did not provide the completed investigation or create/update semantics. Repository fallback was required.

## Smallest Safe Change

Add a pure generator, add `existsBySlug`, allocate only during create, stop copying slug during update, remove both slug fields, add title/form error handling, and cover the behavior with isolated tests. Do not add an index annotation, startup initializer, migration, or data rewrite.
