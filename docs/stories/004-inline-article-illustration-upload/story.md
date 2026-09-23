# Story-004 - Add Inline Article Illustration Upload

Status: Implemented - Awaiting human acceptance

## Context

Story-003 restored article cover management in the Angular administration and validated the existing PNG/JPEG/WebP to WebP pipeline, storage volume, XSRF configuration, and public rendering. The Markdown renderer already treats a standalone image as a semantic figure and maps image alt text to `img[alt]` and the optional image title to `figcaption`.

## Problem

Article authors can write Markdown image syntax, but the active Angular editor does not provide a bounded way to upload an illustration, obtain its public URL, and insert the resulting Markdown while drafting an article that may not yet exist.

## Goal

Allow an administrator to select an inline illustration, provide explicit alt text and an optional caption, upload it through the existing image processor, and insert the generated Markdown at the current content cursor.

## Scope

- Dedicated authenticated multipart illustration upload endpoint.
- Reuse `ArticleImageProcessor` validation, resize, and WebP conversion.
- Store generated assets under a UUID-controlled illustration path in the persistent uploads volume.
- Return only a public URL to Angular.
- Add a subordinate illustration section beside the Markdown editor.
- Insert valid standalone Markdown at the current cursor/selection.
- Preserve the existing `withXsrfConfiguration` setup and cover behavior.
- Add focused backend, Angular, Markdown-contract, and runtime regression tests.

## Out Of Scope

- Media library, asset browser, search, tags, or metadata entity.
- WYSIWYG editor, drag-and-drop framework, cropper, gallery, or image variants.
- Illustration deletion lifecycle, reference scanning, or orphan cleanup.
- AI-generated alt text or captions.
- Cover redesign, Markdown renderer redesign, storage migration, CDN, S3, or responsive variants.

## Architecture

```text
Angular article editor
  file + alt + optional caption
             |
             v
POST /api/admin/articles/illustrations (multipart image)
             |
             v
ArticleService -> ArticleImageProcessor -> FileStorageService
             |
             v
{ "url": "/uploads/articles/illustrations/{UUID}.webp" }
             |
             v
Markdown inserted into Article.content
```

The upload is intentionally independent of an Article ID so it works while creating a new draft. The URL is embedded in Markdown; no database asset record is introduced.

## API Contract

- `POST /api/admin/articles/illustrations` consumes `multipart/form-data`.
- The required part is `image`.
- The endpoint requires `ROLE_ADMIN` and the existing CSRF/XSRF protection.
- A successful response is JSON containing only the public `url`.
- The server validates MIME type, byte size, decoding, pixel count, and processing before storage.
- The frontend `accept` attribute is picker guidance only.
- Invalid input returns a client error; storage/processing failures do not expose filesystem paths.

## Storage Ownership

Illustrations use `uploads/articles/illustrations/{UUID}.webp` and the existing persistent Docker `uploads` volume. UUID filenames are generated server-side. Public GET access is expected through the existing `/uploads/**` resource mapping. Uploaded assets may remain orphaned when an author never inserts or saves them; cleanup is a future lifecycle concern and is deliberately not implemented here.

## Markdown Contract

With caption:

```markdown
![ALT](URL "CAPTION")
```

Without caption:

```markdown
![ALT](URL)
```

The renderer remains unchanged: a standalone image becomes `<figure>`, `ALT` becomes `img[alt]`, and `CAPTION` becomes `<figcaption>`. User text is escaped for the supported Markdown image syntax before insertion.

## UX

- The editor exposes an illustration file picker accepting PNG, JPEG, and WebP.
- Alt text is required and caption is optional.
- `Upload & insert` is disabled until a file and alt text are present.
- Upload success inserts the generated standalone Markdown at the textarea cursor/selection and keeps surrounding content.
- The insertion adds bounded blank-line separation without reformatting the article.
- Upload and validation errors use the existing form error area.
- No illustration delete button is provided in V1.

## Failure Semantics

- A failed upload leaves article content unchanged.
- A failed insertion leaves the selected article fields unchanged.
- An uploaded but unused asset may remain stored.
- Cover create, preserve, replace, and remove behavior remains unchanged.

## Image Invariants

The implementation reuses the existing processor values rather than duplicating them:

```text
formats     = PNG, JPEG, WebP
max upload  = 5 MB
max pixels  = 25 megapixels
max width   = 1200 px
output      = WebP
quality     = 80
```

## Acceptance Criteria

- An admin can upload a valid PNG, JPEG, or WebP from the Angular article editor before saving an article.
- The backend uses `ArticleImageProcessor` and returns a public WebP URL under the illustration path.
- Alt text is required, caption is optional, and neither is inferred from the filename.
- Captioned and uncaptioned Markdown are generated correctly.
- Markdown is inserted at the current cursor/selection with readable standalone spacing.
- The real Angular XSRF configuration sends the token on the multipart upload.
- Invalid MIME, oversized, corrupt, and excessive-pixel images are rejected by backend validation.
- Public article rendering produces the existing figure/alt/figcaption semantics.
- No media entity, media library, deletion lifecycle, or orphan cleaner is introduced.
- Story-003 cover behavior remains regression-tested.

## Testing Strategy

- Angular API tests verify multipart transport and response URL handling.
- Angular component tests verify the input, required alt, optional caption, escaping, cursor insertion, spacing, and error feedback.
- Backend service/controller tests verify processor reuse, illustration storage URL, validation propagation, and admin authorization/CSRF.
- Existing `ArticleImageProcessorTest` and `MarkdownServiceTest` remain part of the regression suite, with focused renderer assertions preserved.
- Docker runtime E2E uses disposable admin/article data to validate upload, public WebP delivery, saved Markdown, and figure semantics.

## Human Acceptance

This Story is complete only after implementation and validation; it then awaits human review and acceptance.
