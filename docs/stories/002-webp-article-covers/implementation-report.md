# Story-002 Implementation Report

## Overview

Article cover uploads are validated and converted to one canonical WebP while existing cover URLs and project uploads remain unchanged. This report was reconciled after an interrupted OpenCode session; claims below reflect the recovered source and fresh verification rather than the pre-crash report.

## Implementation

- Added Thumbnailator 0.4.21 for JPEG EXIF orientation and resizing.
- Added webp4j 2.5.0 for WebP decode and encode at quality 80.
- Enforced a 5 MB multipart and processor limit and a 25 megapixel decoded-image limit.
- Accepts PNG, JPEG, and WebP; rejects empty, unsupported, corrupt, oversized, or over-dimensioned inputs with French messages.
- Normalizes JPEG EXIF orientation before sizing, limits width to 1200px without upscaling, preserves alpha, and strips source metadata through decode/encode.
- Stores only `uploads/articles/{uuid}.webp` and keeps `Article.coverImage` as one URL string.
- Preserves `FileStorageService.save(MultipartFile)` and all project upload behavior.
- Replaces covers in the order new file, DB save, then old pipeline-owned file cleanup.
- Deletes only direct UUID WebP paths owned by `/uploads/articles/`; legacy `/uploads/projects/` paths are never deleted.
- Cleans newly written files best-effort after create/update persistence failures, including slug retry lookup failures.
- Returns article forms with friendly errors when image processing or storage raises `IOException`.

## Recovery Corrections

The interrupted implementation was near complete but not ready to merge:

1. `saveArticleWebP()` wrote below `uploads/articles/`, while deletion resolved below `articles/`; cleanup therefore did not remove stored files.
2. JPEG uploads were decoded to `BufferedImage` before Thumbnailator, so EXIF metadata was unavailable despite reports claiming normalization.
3. Storage tests ignored `@TempDir`, wrote fake WebPs into the repository, and did not assert physical deletion.
4. A duplicate-key retry followed by a repository lookup failure leaked the newly stored cover.
5. Processing/storage `IOException`s escaped the article controller.

All five gaps were corrected without redesigning the existing implementation.

## Verification

- Targeted Story tests: 51 passed, 0 failed.
- Full `./mvnw test`: 71 passed, 0 failed.
- `./mvnw verify`: 71 passed, 0 failed; package succeeded.
- Docker image build: succeeded.
- Docker Compose: PostgreSQL healthy, MongoDB running, application running.
- HTTP: `/` 200, `/blog` 200, `/admin/articles` 302.
- Container WebP JNI probe: encode/decode round trip returned `WEBP_CODEC_OK` on `eclipse-temurin:21-jre`.

## Production Safety

- No database migration or production deployment.
- Existing PNG/JPEG files and URLs are untouched.
- Legacy `/uploads/projects/` files are never auto-deleted.
- The existing named uploads volume and generic `/uploads/**` resource mapping are unchanged.
- Rollback remains data-compatible because prior code can serve stored `.webp` URLs.

## Known Limitations

- Runtime validation used local Docker Compose, not the production VPS.
- Image processing remains synchronous and quality 80 is fixed, as scoped.
- The application-level slug uniqueness retry still has its documented concurrency window.
- Remote branch freshness could not be fetched because SSH key signing was unavailable during recovery.
