# Story-004 Implementation Report

## Overview

Inline article illustrations can be uploaded from the Angular article editor before an article is saved. The backend validates and converts the image through the existing pipeline, stores it under the dedicated illustration path, and returns a public WebP URL. The editor inserts standalone Markdown at the current cursor.

## Implementation

- Added `ArticleIllustrationDto` with the public illustration URL.
- Added authenticated `POST /api/admin/articles/illustrations` multipart endpoint.
- Reused `ArticleImageProcessor` for MIME, size, decode, pixel, resize, and WebP validation.
- Added UUID-controlled illustration storage under `uploads/articles/illustrations/`.
- Added Angular API model and multipart upload method.
- Added the inline illustration form with required alt text, optional caption, upload feedback, and cursor insertion.
- Preserved the existing XSRF configuration and Story-003 article-cover behavior.
- Added backend, Angular, Markdown-contract, and authorization coverage.

## Markdown Contract

Captioned illustrations use:

```markdown
![ALT](URL "CAPTION")
```

Without a caption, the title is omitted. The existing renderer produces `<figure>`, `img[alt]`, and optional `<figcaption>` semantics.

## Validation

```text
ANGULAR_BUILD = PASS (`npm run build`)
ANGULAR_TESTS = PASS, 18 tests, 0 failures
MAVEN_TESTS = PASS, 106 tests, 0 failures
GIT_DIFF_CHECK = PASS (`git diff --check`)
DOCKER_BUILD = PASS (`portfolio`, `frontend`)
```

### Runtime E2E

Validated against rebuilt Docker containers with disposable local data:

```text
ADMIN_LOGIN = PASS
INLINE_UPLOAD = PASS, multipart POST 201
PUBLIC_URL = PASS, UUID WebP illustration path
CONTENT_TYPE = PASS, image/webp
CURSOR_INSERTION = PASS, surrounding Markdown preserved
PUBLIC_API_RENDERING = PASS, figure/alt/figcaption semantics
PUBLIC_BROWSER_RENDERING = PASS, visible figure with caption
CLEANUP = PASS, temporary article, admin, and illustration removed
```

The first upload attempt after container replacement returned `403` while using the already hydrated page. A full page navigation refreshed the Angular runtime/XSRF state, after which the same scenario passed. No source change was made for this transient browser state; it should remain a point for human acceptance review.

## Out Of Scope

- Media entity or media library.
- Illustration deletion lifecycle or orphan cleanup.
- Editor redesign, cropper, gallery, CDN, or storage migration.

## Human Acceptance

```text
IMPLEMENTED - AWAITING HUMAN ACCEPTANCE
```
