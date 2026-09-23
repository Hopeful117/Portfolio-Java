# Story-003 Implementation Report

## Overview

Article cover management is restored in the active Angular administration. Article metadata-only saves remain JSON requests, selected covers use the existing multipart endpoints, and explicit removal uses a new authenticated cover endpoint.

## Implementation

- Added canonical Story-003 documentation in `story.md`.
- Added Angular article cover file selection with PNG/JPEG/WebP picker guidance.
- Added current-cover preview during article editing.
- Added simple selected-file local preview using an object URL.
- Distinguished current cover, selected replacement, and explicit removal state.
- Kept metadata-only article saves on the existing JSON endpoints.
- Reused existing multipart article create/update methods for selected covers.
- Added `DELETE /api/admin/articles/{id}/cover`.
- Added `ArticleService.removeCover()` with database-first persistence and existing owned-asset cleanup rules.
- Preserved `ArticleImageProcessor`, `FileStorageService`, WebP settings, and replacement ordering.
- Removed Angular `withFetch()` from `app.config.ts` so the configured XSRF interceptor sends `X-XSRF-TOKEN` on mutating admin requests.

## Cover Operations

```text
CREATE_WITHOUT_COVER = JSON POST; supported
CREATE_WITH_COVER    = multipart POST; supported
KEEP_EXISTING_COVER  = JSON PUT; preserved
REPLACE_COVER        = multipart PUT; supported
REMOVE_COVER         = JSON metadata PUT followed by DELETE cover; supported
```

Removal is ordered so that the cover reference is cleared successfully before cleanup. If metadata persistence fails, the existing reference and file remain intact. Cleanup remains best effort and only targets pipeline-owned article WebPs.

## Image Pipeline

```text
FORMATS = PNG, JPEG, WebP
MAX_UPLOAD = 5 MB
MAX_PIXELS = 25 megapixels
MAX_WIDTH = 1200 px
OUTPUT = WebP
QUALITY = 80
```

`ArticleImageProcessor` was not modified.

## Security

```text
ADMIN_AUTH_PRESERVED = yes
BACKEND_VALIDATION_PRESERVED = yes
CLIENT_PATH_CONTROL_INTRODUCED = NO
```

The new endpoint remains under `/api/admin/**`; existing role authorization and CSRF behavior remain active.

## Validation

### Angular

```text
FOCUSED = not applicable; full browser suite executed
FULL = PASS, 15 tests, 0 failures
BUILD = PASS (`npm run build`)
```

The first attempt without an explicit browser path failed because Karma could not find ChromeHeadless. Investigation found a working Playwright-managed browser at `/home/ludo/.cache/ms-playwright/chromium-1243/chrome-linux64/chrome`. Running with `CHROME_BIN` set to that executable completed all tests successfully under Chrome Headless 153.

The frontend browser suite was rerun after the XSRF configuration fix:

```text
RESULT = PASS, 15 tests, 0 failures
```

### Spring

```text
FOCUSED = PASS, 28 tests, 0 failures
FULL = PASS, 101 tests, 0 failures
```

Focused coverage includes ArticleService removal semantics, controller response, and anonymous admin authorization.

### Image Processor Regression

```text
RESULT = PASS within full Maven suite, 13 tests, 0 failures
```

### Diff

```text
GIT_DIFF_CHECK = PASS
```

## Runtime Validation

```text
PUBLIC_HOME = PASS, HTTP 200
PUBLIC_PROJECTS = PASS, HTTP 200
PUBLIC_BLOG = PASS, HTTP 200
PUBLIC_SKILLS = PASS, HTTP 200
PUBLIC_CAREER = PASS, HTTP 200
PUBLIC_CONTACT = PASS, HTTP 200
PUBLIC_ARTICLE = PASS, HTTP 200
ADMIN_LOGIN = PASS, authenticated as a temporary local admin fixture
CONSOLE_ERRORS = 0
CREATE_WITH_COVER = PASS, multipart POST 201, WebP preview persisted
KEEP_COVER = PASS, JSON PUT 200, existing WebP remained available
REPLACE_COVER = PASS, multipart PUT 200, WebP path changed
REMOVE_COVER = PASS, JSON PUT 200 followed by DELETE cover 200, preview removed
DELETE_TEST_ARTICLE = PASS, DELETE 204
DELETE_TEST_COVER_FILES = PASS, both old WebP URLs returned 404
DELETE_TEST_PUBLIC_API = PASS, temporary slug returned 404
```

The existing Nginx container was stopped because it referenced an obsolete host path from a previous workspace location. It was recreated with the current compose path; application data was not modified. For this admin validation, the application and frontend images were rebuilt locally with the current code. A temporary PostgreSQL admin user and temporary article were created, then both the user and article were deleted after the test.

## Files Modified

- `docs/stories/003-restore-article-cover-management/story.md`
- `docs/stories/003-restore-article-cover-management/implementation-report.md`
- `frontend/src/app/core/admin-api.service.ts`
- `frontend/src/app/core/admin-api.service.spec.ts`
- `frontend/src/app/app.config.ts`
- `frontend/src/app/pages/admin-workspace.page.ts`
- `frontend/src/app/pages/admin-workspace.page.spec.ts`
- `src/main/java/com/hopefull117/portfolio/java/controller/AdminApiController.java`
- `src/main/java/com/hopefull117/portfolio/java/service/ArticleService.java`
- `src/test/java/com/hopefull117/portfolio/java/controller/AdminApiControllerTest.java`
- `src/test/java/com/hopefull117/portfolio/java/controller/AdminApiSecurityTest.java`
- `src/test/java/com/hopefull117/portfolio/java/service/ArticleServiceTest.java`

## Out Of Scope

```text
BODY_ILLUSTRATION_UPLOAD = NO
MEDIA_LIBRARY = NO
BLOG_REDESIGN = NO
IMAGE_EDITOR = NO
STORAGE_MIGRATION = NO
```

## Remaining Findings

- The default `npm test -- --watch=false --browsers=ChromeHeadless` invocation does not discover the Playwright-managed browser unless `CHROME_BIN` is exported in this shell environment.
- No permanent automated E2E suite or admin fixture exists in the repository; this validation used a temporary browser-driven fixture outside version control.

## Human Acceptance

```text
IMPLEMENTED - AWAITING HUMAN ACCEPTANCE
```
