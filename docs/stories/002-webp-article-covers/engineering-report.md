# Story-002 Engineering Report

## Summary

New article cover uploads are converted synchronously to one optimized WebP. PNG, JPEG, and WebP inputs are limited to 5 MB and 25 megapixels, JPEG EXIF orientation is normalized, images wider than 1200px are downscaled, and output is encoded at quality 80. Existing images, project uploads, URLs, schema, and Docker volumes are unchanged.

This Story was recovered after an interrupted OpenCode session. Repository evidence showed that most implementation and documentation existed only as uncommitted work; several completion claims were incorrect and were repaired before final verification.

## DevLog Evidence

- MCP available: yes.
- Project: `portfolio`.
- Freshness: `NO_BASELINE`; warning `PROJECT_CONTEXT_STALE`.
- Observed revision: `88f98f6`, default branch only.
- Useful evidence: investigation merge and later default-branch CI history.
- Branch limitation: DevLog could not see uncommitted Story-002 source or tests.
- Repository fallback: required and authoritative for all recovery conclusions.

## Processing Pipeline

```text
MultipartFile
  -> MIME and 5 MB validation
  -> decode (Thumbnailator for JPEG orientation, ImageIO for PNG, webp4j for WebP)
  -> 25 MP validation
  -> downscale to <=1200px width when required
  -> webp4j encode at quality 80
  -> uploads/articles/{uuid}.webp
  -> Article.coverImage URL
```

Decode and re-encode strips source metadata. ARGB input remains transparent through WebP encoding.

## Consistency Model

### Create

Process and store the new WebP, then save the article. Any persistence failure triggers best-effort deletion of the new owned asset while preserving the original exception.

### Replace

Store the new WebP, update and save the article, then delete the previous cover only when it is a pipeline-owned UUID WebP. A failed save cleans the new file and leaves the persisted old reference usable.

### Delete

Delete the article first, then clean its pipeline-owned cover. Legacy project paths are never cleanup candidates.

## Requirement Status

| Requirement | Final state |
|---|---|
| PNG/JPEG/WebP to WebP q80 | Done |
| 5 MB and 25 MP limits | Done |
| Width limit without upscale | Done |
| JPEG EXIF orientation | Done and tested |
| Alpha preservation and metadata stripping | Done |
| One article file and one URL | Done |
| Safe replacement/orphan cleanup | Done and filesystem-tested |
| Legacy and project behavior unchanged | Done |
| French validation/I/O feedback | Done |
| Docker/JNI compatibility | Verified locally |
| Production migration | None |

## Verification

```text
./mvnw -Dtest=ArticleImageProcessorTest,FileStorageServiceTest,ArticleServiceTest,AdminArticleControllerTest test
  51 tests, 0 failures, 0 errors

./mvnw test
  71 tests, 0 failures, 0 errors

./mvnw verify
  71 tests, package success

docker compose build portfolio
  success

docker compose up -d
  PostgreSQL healthy; MongoDB and application running
```

Manual HTTP checks returned 200 for `/` and `/blog`, and the protected admin route returned 302. A WebP encode/decode probe executed inside the application container returned `WEBP_CODEC_OK`, directly validating the bundled JNI codec on the runtime image.

## Recovery Hygiene

- Removed malformed root files created by interrupted shell redirections.
- Removed fake article WebPs created by the old storage tests.
- Preserved the unrelated pre-existing project upload and ignored local `.env`.
- No secret was added to tracked or Story artifacts.
- No production deployment, merge, data migration, or volume deletion occurred.

## Limitations

- Production VPS and production data were not accessed.
- Processing remains synchronous and WebP quality is fixed by design.
- Remote refs could not be refreshed because SSH key signing failed; local branch base remains `28a2536` while DevLog reports newer default-branch history.
