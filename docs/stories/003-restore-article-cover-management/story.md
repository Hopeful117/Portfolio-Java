# Story-003 - Restore Article Cover Management in Angular Admin

Status: Implemented - Awaiting human acceptance

## Context

The active Portfolio administration is the Angular workspace at `/admin`. The historical Thymeleaf article forms still support multipart cover uploads, and the Angular-facing Spring API already supports JSON article metadata plus multipart article create/update requests.

The Angular article form lost its cover controls during the administration migration. The response model still exposes `coverImage`, but the active article form has no file input, preview, replacement action, or removal action.

## Goal

Restore bounded article cover management in the active Angular administration without redesigning article media management or duplicating the existing image pipeline.

## Scope

- Optional cover selection during article creation.
- Current cover preview during article editing.
- Metadata-only saves through the existing JSON endpoints.
- Cover create/replacement through the existing multipart endpoints.
- Explicit cover removal through `DELETE /api/admin/articles/{id}/cover`.
- Existing WebP processing, storage, cleanup, authorization, and CSRF behavior.
- Focused Angular and Spring regression tests.

## Out Of Scope

- Body illustration upload.
- Media library, asset browser, folders, tagging, or search.
- Cropper, image editor, drag-and-drop upload framework, or WYSIWYG editor.
- Blog-card redesign.
- Responsive image variants, CDN, S3, or storage migration.
- Client-side image conversion.
- Project-image refactoring.

## Architecture

```text
Angular Article Admin
  metadata only       -> JSON article endpoint
  cover selected      -> existing multipart article endpoint
  cover removal       -> DELETE /api/admin/articles/{id}/cover
                               |
                               v
                         ArticleService
                               |
                         existing ArticleImageProcessor
                               |
                         FileStorageService
                               |
                         Article.coverImage
```

The frontend distinguishes the existing cover, a selected replacement file, and an explicit removal request. Absence of a selected file means preserve the current cover unless removal was explicitly requested.

## API Behavior

- `POST /api/admin/articles` with JSON creates an article without a cover.
- `POST /api/admin/articles` with multipart `data` and optional `image` creates an article and processes the image through the existing pipeline.
- `PUT /api/admin/articles/{id}` with JSON updates metadata and preserves the current cover.
- `PUT /api/admin/articles/{id}` with multipart `data` and `image` replaces the cover using the existing safe replacement ordering.
- `DELETE /api/admin/articles/{id}/cover` clears `Article.coverImage`, persists the article, then best-effort cleans a pipeline-owned WebP.
- Cover removal remains protected by the existing `/api/admin/**` role and CSRF rules.

## Image Pipeline Invariants

The Story does not change `ArticleImageProcessor` or its behavior:

```text
formats     = PNG, JPEG, WebP
max upload  = 5 MB
max pixels  = 25 megapixels
max width   = 1200 px
output      = WebP
quality     = 80
storage     = uploads/articles/{UUID}.webp
```

Legacy cover paths remain renderable and are never deleted by the new removal operation unless existing ownership rules identify the path as a pipeline-owned article WebP.

## UX Behavior

- The article form offers a cover file picker accepting PNG, JPEG, and WebP.
- New articles may be saved without a cover.
- Editing an article displays its current cover when present.
- A selected file shows a local preview before save.
- Editing can keep, replace, or explicitly remove the current cover.
- Validation and processing failures are shown through the existing form error area.
- After a successful operation, the article list is reloaded so persisted cover state is reflected.

## Failure Semantics

- Metadata-only update failure leaves the current cover unchanged.
- Replacement keeps the existing service ordering: store/process the new image, persist the new reference, then clean the old owned asset.
- Cover removal persists the cleared reference before attempting filesystem cleanup.
- If persistence fails during removal, the existing cover reference is retained and no cleanup is attempted.
- Cleanup remains best effort and does not restore an invalid database reference after successful persistence.
- Backend validation remains authoritative; browser `accept` is only picker guidance.

## Acceptance Criteria

- Angular article creation supports an optional cover and still supports coverless creation.
- Angular article editing displays an existing cover when present.
- Metadata-only editing preserves the existing cover.
- A selected cover uses the existing multipart create/update endpoint.
- A replacement uses the existing WebP pipeline and safe cleanup behavior.
- Explicit removal calls the cover removal endpoint and clears the persisted cover reference.
- Removal protects legacy or unowned paths from destructive deletion.
- Current cover state is reflected after successful operations without a browser reload.
- Existing Angular public article rendering remains correct for present and null covers.
- Existing admin authorization, CSRF protection, MIME validation, size limits, and path safety remain intact.
- No body illustration or media-library capability is introduced.

## Testing Strategy

- Angular service tests verify JSON, multipart, and cover-removal transport behavior.
- Angular component tests verify the article cover input, current-cover preview, selected-file preview, and explicit removal state.
- Spring controller tests verify the cover-removal endpoint and existing multipart article routes.
- `ArticleService` tests verify successful removal, owned-file cleanup, legacy protection, and persistence-failure behavior.
- Existing `ArticleImageProcessor` regression tests are run unchanged.
- Existing public article tests/builds verify present and null cover rendering behavior.

## Human Acceptance

Implementation is complete for this bounded Story and awaits human review and acceptance. Inline article illustration upload remains a separate future capability.
