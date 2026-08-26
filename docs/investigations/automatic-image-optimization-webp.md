# Automatic Image Optimization and WebP Conversion - Investigation

Status: Complete

Evidence labels used throughout this report:

- **OBSERVED**: directly verified in repository history, source, configuration, or tests.
- **INFERRED**: conclusion supported by observed evidence but not directly encoded.
- **PROPOSED**: recommended future behavior; not implemented.
- **NOT VERIFIED**: unavailable or not established during this investigation.

# Executive Summary

- **OBSERVED**: Portfolio has exactly two admin-uploaded image use cases: article cover images (MongoDB) and project screenshots (PostgreSQL). Both flow through a shared `FileStorageService` that writes to `uploads/projects/` with no image processing, validation, or format conversion.
- **OBSERVED**: Article covers are the primary performance pain point. They render as `<img>` tags in `blog.html` (card grid) and `article.html` (hero image) -- the highest-traffic public pages. PNG originals at 64KB each are served without optimization.
- **OBSERVED**: No MIME type validation exists. Any file type is accepted. No file size validation beyond Spring Boot defaults (1MB). No image dimensions validation.
- **OBSERVED**: Old files are never deleted on image replacement or article/project deletion, causing storage leaks. 8 orphan PNGs exist in `uploads/projects/`.
- **OBSERVED**: Technology icons are external URLs (not uploads) -- out of scope.
- **PROPOSED**: Implement WebP conversion for article cover images only (initially). Convert uploaded PNG/JPEG/WebP to optimized WebP using Java's `ImageIO` or a lightweight library. Store converted files in a new `uploads/articles/` subdirectory. Apply quality 80, max width 1200px, strip metadata.
- **PROPOSED**: Add MIME type validation (accept only `image/png`, `image/jpeg`, `image/webp`). Add explicit file size limit (e.g., 5MB). Add file cleanup on replacement and deletion.
- **PROPOSED**: Keep original file as fallback; serve WebP via `<picture>` element with `<source type="image/webp">` and `<img>` fallback for older browsers.
- **NOT VERIFIED**: Browser WebP support on Portfolio's actual visitor base. Chrome/Firefox/Edge/Safari all support WebP as of 2024; the fallback is defensive.

# Current Image Lifecycle

## Article Cover Image - Create

1. **OBSERVED**: `GET /admin/articles/add` renders `admin/articles/form-articles` with a blank `Article` (`AdminController.java:241-251`).
2. **OBSERVED**: The form has `<input type="file" name="image">` at `form-articles.html:106-109`. No `accept` attribute restricts file type.
3. **OBSERVED**: `POST /admin/articles` receives `@RequestParam("image") MultipartFile image` (`AdminController.java:259`) and calls `articleService.create(article, image)` (`AdminController.java:268`).
4. **OBSERVED**: `ArticleService.create()` checks `!file.isEmpty()` and calls `fileStorageService.save(file)` (`ArticleService.java:64-66`).
5. **OBSERVED**: `FileStorageService.save()` generates filename `UUID.randomUUID() + "_" + file.getOriginalFilename()`, copies stream to `uploads/projects/`, returns URL `/uploads/projects/{filename}` (`FileStorageService.java:16-26`).
6. **OBSERVED**: The URL string is stored in `Article.coverImage` (`Article.java:36`) in MongoDB `articles` collection.

## Article Cover Image - Edit

1. **OBSERVED**: `GET /admin/articles/edit/{id}` loads the existing `Article` and renders `form-edit-articles` (`AdminController.java:281-294`).
2. **OBSERVED**: The form has `<input type="file" name="image">` at `form-edit-articles.html:122-125`. No preview of current cover image.
3. **OBSERVED**: `POST /admin/articles/edit/{id}` receives a new `MultipartFile image` (`AdminController.java:301`) and calls `articleService.update(id, article, image)` (`AdminController.java:308`).
4. **OBSERVED**: `ArticleService.update()` checks `!file.isEmpty()`. If a new file is selected, saves it and overwrites `existingArticle.coverImage`. If no file is selected, the existing path is preserved (`ArticleService.java:101-103`).
5. **OBSERVED**: **The old file on disk is NEVER deleted.** The previous cover image becomes an orphan.

## Article Cover Image - Public Display

1. **OBSERVED**: `GET /blog` lists published articles via `articleService.findPublished()` and renders `public/blog.html` (`ArticleController.java:24-31`).
2. **OBSERVED**: Blog card renders `<img th:src="${article.coverImage}" th:alt="${article.title}">` (`blog.html:52`). Null-guarded with `th:if`.
3. **OBSERVED**: `GET /blog/{slug}` loads article via `articleService.findBySlug(slug)` and renders `public/article.html` (`ArticleController.java:38-49`).
4. **OBSERVED**: Article hero renders `<img th:src="${article.coverImage}" th:alt="${article.title}">` (`article.html:77`). Null-guarded.

## Project Image - Create/Edit/Display

1. **OBSERVED**: `ProjectService.create()` saves image via `fileStorageService.save()` and stores URL in `Project.imagePath` (`ProjectService.java:42-46`).
2. **OBSERVED**: `ProjectService.updateFromDto()` same pattern, old file never deleted (`ProjectService.java:71-74`).
3. **OBSERVED**: Public display at `projects.html:38`: `<img th:src="${project.imagePath}" th:alt="${project.title}">`.

## Technology Icon - Out of Scope

1. **OBSERVED**: `Technology.iconeUrl` is a URL text field, not a file upload (`Technology.java:20`).
2. **OBSERVED**: Admin form uses `<input type="url">` (`form-technologies.html:67-70`).
3. **OBSERVED**: Displayed in `home.html:99`, `projects.html:67`.
4. **INFERRED**: External icons (typically SVGs from CDN) are not candidates for local WebP conversion.

# Current Storage Architecture

## FileStorageService

- **OBSERVED**: Single `save(MultipartFile)` method. No `delete()`, no `update()`, no `exists()` (`FileStorageService.java:1-27`).
- **OBSERVED**: Storage root: `Paths.get("uploads/projects")` -- relative to JVM working directory (`FileStorageService.java:14`).
- **OBSERVED**: Filename: `UUID.randomUUID() + "_" + originalFilename` (`FileStorageService.java:20`).
- **OBSERVED**: Returns URL string: `/uploads/projects/{filename}` (`FileStorageService.java:25`).
- **OBSERVED**: Auto-creates directories on save (`FileStorageService.java:18`).
- **INFERRED**: The service is a thin wrapper around `Files.copy()` with no abstraction for format, size, or cleanup.

## WebConfig - Resource Serving

- **OBSERVED**: `WebConfig.java:13-14`: `registry.addResourceHandler("/uploads/**").addResourceLocations("file:uploads/")`.
- **OBSERVED**: Maps URL path `/uploads/**` to filesystem `uploads/` (relative).
- **INFERRED**: Any file placed under `uploads/` (including subdirectories) is publicly accessible via `/uploads/{path}`.

## Security

- **OBSERVED**: `SecurityConfig.java:29-38`: `permitAll()` includes `/css/**`, `/js/**`, `/images/**` but NOT `/uploads/**`.
- **OBSERVED**: `SecurityConfig.java:41-42`: `anyRequest().permitAll()` -- catch-all permits uploaded files.
- **INFERRED**: If security is tightened, `/uploads/**` must be explicitly added to `permitAll()`.

## Docker Deployment

- **OBSERVED**: `docker-compose.yml:66`: `uploads:/app/uploads` -- named volume mounts to container `/app/uploads`.
- **OBSERVED**: `Dockerfile:12`: `WORKDIR /app` -- resolves relative paths to `/app/uploads/projects/`.
- **OBSERVED**: Volume persists across container restarts and rebuilds.
- **NOT VERIFIED**: Volume backup, size limits, or monitoring.

## nginx

- **OBSERVED**: `nginx.conf:5-9`: Proxies all traffic to `http://app:8080`. No static file serving or caching for uploads.

# Current Format Support

- **OBSERVED**: `FileStorageService.save()` accepts any `MultipartFile` -- no content-type validation.
- **OBSERVED**: No `accept` attribute on HTML file inputs (`form-articles.html:106`, `form-edit-articles.html:122`).
- **OBSERVED**: No `spring.servlet.multipart.max-file-size` configured in `application.properties` -- Spring Boot default (1MB) applies.
- **OBSERVED**: Templates render `<img>` with raw `src` path. No `<picture>` element, no `srcset`, no `loading="lazy"`.
- **INFERRED**: Browsers request the raw PNG/JPEG file. No WebP negotiation occurs at the application level. Browsers that support Content Negotiation (via `Accept: image/webp`) would receive WebP if served by a CDN or reverse proxy, but nginx is configured as a pure proxy with no image optimization.

# Proposed Target Behavior (V1 Scope)

## Scope: Article Cover Images Only

**Rationale**: Article covers are the highest-traffic image use case. They appear in the blog listing (card grid) and article detail (hero). Optimizing these delivers the most performance impact per unit of effort. Project images are secondary. Technology icons are external URLs.

## Upload Pipeline Changes

1. **MIME type validation**: Accept only `image/png`, `image/jpeg`, `image/webp`. Reject others with a user-friendly error.
2. **File size validation**: Add explicit limit (e.g., 5MB) in `application.properties` and/or service code.
3. **WebP conversion**: Convert uploaded PNG/JPEG/WebP to optimized WebP (quality 80, max width 1200px, strip metadata). Store in `uploads/articles/` (new subdirectory).
4. **Original retention**: Keep the original file as a fallback for browsers without WebP support.
5. **Database field**: Store both WebP and original paths in `Article.coverImage` (or a new field like `coverImageWebP`).
6. **File cleanup**: Delete old files (both original and WebP) on image replacement and article deletion.

## Display Pipeline Changes

1. **`<picture>` element**: Use `<picture>` with `<source type="image/webp" srcset="...">` and `<img>` fallback for PNG/JPEG.
2. **Apply to**: `blog.html:49-55` (card cover) and `article.html:73-81` (hero cover).
3. **No `loading="lazy"` in V1**: Can be added later; not part of this investigation.

## Engineering Constraints

1. **Production deployment**: VPS with Docker Compose. No downtime.
2. **No external dependencies**: Use Java's built-in `ImageIO` or a well-established lightweight library (e.g., `thumbnailator`, `webp-imageio`).
3. **No database migration**: Store additional path as a new optional field (default null). Existing articles with only `coverImage` continue to work.
4. **Backward compatibility**: Existing PNG/JPEG URLs must not break. New WebP URLs are additive.
5. **No CDN changes**: nginx remains a pure proxy. WebP conversion happens at upload time.

# Risks and Open Questions

1. **ImageIO WebP support**: Java's `ImageIO` does not natively support WebP encoding. A library like `webp-imageio` (Google's encoder) or `thumbnailator` with WebP support is needed. Must verify license compatibility (Apache 2.0 for both).
2. **Thumbnail quality**: Max width 1200px and quality 80 are starting points. Real-world testing on Portfolio's actual article covers is needed.
3. **Original retention**: Storing both original and WebP doubles storage per article cover. On a small portfolio this is acceptable; on a content-heavy site it may not be.
4. **Old articles**: No backfill migration is proposed. Existing articles keep their PNG/JPEG originals. New uploads get WebP conversion. A future story could batch-convert existing covers.
5. **Orphan file cleanup**: The proposed file cleanup is orthogonal to WebP conversion but should be addressed in the same story to avoid compounding the leak.

# References

- `FileStorageService.java` -- lines 14, 16-26 (storage root, save method)
- `ArticleService.java` -- lines 51-93 (create), 97-126 (update), 141 (delete)
- `AdminController.java` -- lines 256-278 (create article), 297-316 (edit article)
- `WebConfig.java` -- lines 11-14 (resource handler)
- `SecurityConfig.java` -- lines 28-43 (permitAll)
- `docker-compose.yml` -- lines 65-66 (volume mount), 73 (volume declaration)
- `nginx.conf` -- lines 5-9 (proxy all)
- `form-articles.html` -- lines 106-109 (file input)
- `form-edit-articles.html` -- lines 122-125 (file input)
- `blog.html` -- lines 49-55 (card cover image)
- `article.html` -- lines 73-81 (hero cover image)
- `application.properties` -- no multipart config (Spring Boot defaults)
