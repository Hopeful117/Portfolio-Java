# Story-002 - Automatically Optimize New Article Cover Uploads as WebP

Status: Complete

## Goal

Stop manually resizing/compressing/converting article covers in GIMP. Automatically validate, decode, resize, and encode new article cover uploads as optimized WebP files while preserving all legacy image URLs and current upload storage behavior.

## Investigation Input

This Story implements the completed investigation at `docs/investigations/automatic-image-optimization-webp.md`. Current source must remain authoritative if it differs from that report.

## Acceptance Criteria

- New article cover uploads (PNG, JPEG, WebP) are validated, decoded, resized if oversized, and encoded as WebP.
- Files exceeding 5MB are rejected with a French error message.
- Decoded images exceeding 25 megapixels are rejected.
- Images wider than 1200px are downscaled without upscaling smaller images.
- EXIF orientation is normalized where supported.
- Output is one canonical WebP at quality 80.
- Alpha transparency is preserved (PNG transparency → WebP transparency).
- Unnecessary metadata is stripped.
- Only one file is stored under `/uploads/articles/`.
- `Article.coverImage` continues to store one public URL.
- No template migration is necessary.
- Existing PNG/JPEG production images remain untouched.
- Legacy `/uploads/projects` files are never auto-deleted.
- Replacement never deletes the old active cover before the new one is safely persisted.
- Failed replacement leaves the existing article and cover usable.
- New orphan files are cleaned best-effort when DB persistence fails.
- Pipeline-owned old WebPs can be cleaned safely after successful replacement.
- Project image uploads remain unchanged.
- Docker/JNI runtime compatibility is verified.
- Tests cover both new and legacy behavior.
- Rollback remains compatible with stored WebP URLs.
- No production data migration occurs.

## Scope

- Article cover uploads only.
- PNG / JPEG / WebP inputs.
- Validation (MIME type, file size, pixel dimensions).
- EXIF orientation normalization.
- Downscale when oversized (>1200px width).
- WebP encoding (quality 80).
- One final stored `.webp` under `uploads/articles/`.
- Safe filename generation (UUID).
- Safe replacement behavior (old deleted after new is active).
- Cleanup only for assets owned by the new pipeline.
- User-facing French validation errors.
- Automated tests.
- Docker runtime validation.
- Production-safety documentation.

## Non-Scope

- Project screenshot conversion.
- Technology icons.
- Migration of existing images.
- Bulk WebP backfill.
- Cleanup of historical orphan files.
- CDN / object storage.
- AVIF / responsive variants.
- Crop/editor UI.
- Async workers / image queues.
- AI image processing.
- Template redesign.
- Generic MediaPipeline abstraction.

## Production Constraint

Portfolio is a LIVE PRODUCTION APPLICATION deployed on a VPS and publicly accessible. Treat main as a PRODUCTION branch. Do NOT assume access to the production VPS or production upload volume. Do NOT mutate historical production assets.
