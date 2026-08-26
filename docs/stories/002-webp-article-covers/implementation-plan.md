# Story-002 Implementation Plan

## Implementation Steps

1. Add Maven dependencies: `net.coobird:thumbnailator:0.4.21` and `dev.matrixlab.webp4j:webp4j-core:2.5.0`
2. Configure `spring.servlet.multipart.max-file-size=5MB` and `spring.servlet.multipart.max-request-size=5MB` in `application.properties`
3. Create `ProcessedImage` record (byte[] data, int width, int height)
4. Create `ArticleImageProcessor` component with validation, decode, resize, WebP encode
5. Add `saveArticleWebP(byte[])` and `deleteArticleAsset(String)` to `FileStorageService`
6. Update `ArticleService.create()` to process image through `ArticleImageProcessor` before storage
7. Update `ArticleService.update()` with safe replacement ordering (store new → save DB → delete old)
8. Update `ArticleService.deleteById()` with pipeline-owned file cleanup
9. Add `accept` attribute to article form file inputs
10. Add French validation error handling in `AdminController`
11. Write tests for `ArticleImageProcessor`
12. Write tests for `FileStorageService` article methods
13. Update `ArticleServiceTest` with image processing flow tests
14. Update `AdminArticleControllerTest` with image validation tests
15. Run `./mvnw test` and verify all tests pass
16. Docker build and runtime validation
17. Create implementation artifacts (report, review, engineering report)

# Production Safety

## Existing Production Images
UNTOUCHED. No scanning, converting, renaming, or deletion of historical files.

## Existing URLs
UNCHANGED. Legacy `.png`/`.jpg` coverImage values continue rendering via `<img>`.

## Database Migration
NONE. `Article.coverImage` remains a String field.

## Docker Volume
UNCHANGED. `uploads` named volume persists as-is.

## Dockerfile
UNCHANGED. webp4j bundles native libs; no OS package needed.

## Legacy `/uploads/projects`
NEVER auto-deleted. Only files under `/uploads/articles/` owned by the new pipeline may be cleaned.

## New `/uploads/articles`
Pipeline-owned. Created by `ArticleImageProcessor` + `FileStorageService.saveArticleWebP()`.

## Rollback
Application rollback is data-compatible. Old application serves `/uploads/**` generically. WebP URLs stored in `Article.coverImage` render through existing `<img>` tags. No irreversible transformation.

## Native Codec
Validated in Docker via `docker compose build` + runtime test.

## Potential Orphan Scenarios
Documented in consistency report below.

## VPS Access
Do not assume. All validation via local + Docker.

# Filesystem / Database Consistency

There is no distributed transaction. Behavior for each failure scenario:

**A. Processor fails**: Article not persisted. No file written. No cleanup needed.

**B. File write fails**: Article not persisted. No cleanup needed.

**C. DB create fails after file write**: Attempt best-effort cleanup of newly created WebP. Log cleanup failure. Do not hide original error.

**D. DB update fails after new file write**: Old article still references old cover. Clean newly-created WebP best-effort. Log cleanup failure.

**E. Old file cleanup fails after successful update**: New article remains valid. Log cleanup failure. Do NOT rollback to broken state.

**F. Article deletion succeeds but file cleanup fails**: Article removed from DB. Orphan WebP logged. No data inconsistency.

**G. File cleanup succeeds but DB delete fails**: Prefer ordering where DB delete happens first (Article deleted → then file cleanup). This avoids orphan references.

# Replacement Ordering (CRITICAL)

```
existingArticle has oldCover
        ↓
process new upload
        ↓
store new WebP
        ↓
set new cover URL on existingArticle
        ↓
save Article successfully
        ↓
ONLY THEN attempt cleanup of oldCover IF owned by new pipeline
```

# Delete Ordering

```
delete Article from DB
        ↓
if coverImage is under /uploads/articles/:
    attempt cleanup of owned WebP
if coverImage is legacy /uploads/projects/*:
    do NOT delete file
```

# Compression Evidence (预期)

| Input | Dimensions | Output | Reduction |
|-------|-----------|--------|-----------|
| JPEG 3MB | 4000x3000 | ~200KB WebP q80 @1200px | ~93% |
| PNG 640KB | 800x600 | ~80KB WebP q80 | ~87% |
| PNG alpha 200KB | 500x500 | ~50KB WebP q80 | ~75% |
