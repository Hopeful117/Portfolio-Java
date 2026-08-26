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
- **PROPOSED**: Implement WebP conversion for article cover images only. Convert uploaded PNG/JPEG/WebP to one optimized WebP under `uploads/articles/` at quality 80 and a maximum width of 1200px.
- **PROPOSED**: Add MIME, 5MB byte-size, and 25-megapixel validation, plus safe cleanup on replacement and deletion.
- **PROPOSED**: Keep only the canonical WebP and continue using the existing `<img>` rendering; do not migrate legacy covers.
- **NOT VERIFIED**: Browser WebP support on Portfolio's actual visitor base. Broad current browser support made a separate fallback outside the selected scope.

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

## Upload Pipeline (APPROVED - single WebP output)

1. **MIME type validation**: Accept only `image/png`, `image/jpeg`, `image/webp`. Reject others with a user-friendly error.
2. **File size validation**: Explicit limit (5MB) in application code.
3. **Decode**: Load uploaded image into `BufferedImage` using Thumbnailator for oriented JPEG, ImageIO for PNG, and webp4j for WebP.
4. **Dimension safety**: Reject images exceeding pixel safety limit (25 megapixels) to prevent decompression bombs.
5. **Resize**: If width > 1200px, downscale preserving aspect ratio using Thumbnailator. No upscale.
6. **WebP encode**: Convert to WebP using webp4j with quality 0.80. Alpha preserved.
7. **Store ONE file**: Write single `.webp` file to `uploads/articles/`. Return URL path.
8. **Database**: Store single URL string in `Article.coverImage`.
9. **Original file**: NOT retained after successful processing.

## Display Pipeline

1. **No template changes**: Existing `<img th:src="${article.coverImage}">` continues unchanged.
2. **Legacy articles**: `coverImage` points to `.png`/`.jpg` — browsers render normally.
3. **New articles**: `coverImage` points to `.webp` — browsers render normally.
4. **No `<picture>` fallback**: Not needed. 97%+ browser WebP support. Legacy images remain untouched.

## File Cleanup

1. **On replacement**: Delete the old pipeline-owned `.webp` only after the new file is stored and the database update succeeds.
2. **On deletion**: Delete the `.webp` file from `uploads/articles/`.
3. **Legacy files**: `/uploads/projects/*` must NOT be automatically deleted.

## Engineering Constraints

1. **Production deployment**: VPS with Docker Compose. No downtime.
2. **Libraries**: Thumbnailator (resize) + webp4j (WebP codec). Both are Maven dependencies only.
3. **No database migration**: Continue storing one URL in the existing optional `coverImage` field.
4. **Backward compatibility**: Existing PNG/JPEG URLs must not break. New WebP URLs are additive.
5. **No CDN changes**: nginx remains a pure proxy. WebP conversion happens at upload time.

# Library Decision

## Evaluated Options

| # | Library | Type | WebP Read | WebP Write | Alpha | Quality | Resize | License | Maintained | Java 21 | Native |
|---|---------|------|-----------|------------|-------|---------|--------|---------|------------|---------|--------|
| A | TwelveMonkeys `imageio-webp` | ImageIO SPI | YES | **NO** | Read only | N/A | ResampleOp | BSD | YES (3.14.0, Jul 2026) | YES | NO |
| B | webp-imageio (sejda-pdf) | ImageIO JNI | YES | YES | YES | YES | NO | Apache 2.0 | **NO** (0.2.2, Aug 2021) | Risk | YES (bundled) |
| C | webp4j | JNI codec | YES | YES | YES | YES | NO | MIT | YES (2.5.0, Jun 2026) | YES | YES (bundled) |
| D | NightMonkeys `imageio-webp` | ImageIO JNI | YES | **NO** | YES | N/A | NO | MIT | YES (1.1.0, Aug 2025) | **NO (22+)** | YES |
| E | Thumbnailator | Resize library | N/A | N/A | YES | N/A | YES | MIT | YES (0.4.21, Oct 2025) | YES | NO |
| F | JDeli | Commercial | YES | YES | YES | YES | YES | Commercial | YES | YES | NO |

## Selected Stack

**Thumbnailator** (resize + EXIF orientation) + **webp4j** (WebP encode/decode)

### Responsibilities

```
Upload (PNG/JPEG/WebP)
    ↓
Thumbnailator — decode and orient JPEG
ImageIO.read() — decode PNG (built-in)
webp4j.decodeImage() — decode WebP (if needed)
    ↓
BufferedImage
    ↓
Thumbnailator — resize if width > 1200px
    ↓
BufferedImage (resized)
    ↓
webp4j.encodeImage() — encode to WebP (quality 0.80)
    ↓
byte[] → write to uploads/articles/{uuid}.webp
```

### Maven Dependencies

```xml
<!-- Resize + EXIF orientation (pure Java) -->
<dependency>
    <groupId>net.coobird</groupId>
    <artifactId>thumbnailator</artifactId>
    <version>0.4.21</version>
</dependency>

<!-- WebP encode/decode (JNI to libwebp 1.6.0, bundled native libs) -->
<dependency>
    <groupId>dev.matrixlab.webp4j</groupId>
    <artifactId>webp4j-core</artifactId>
    <version>2.5.0</version>
</dependency>
```

### Licence

- **Thumbnailator**: MIT License
- **webp4j**: MIT License

### Java 21 Compatibility

- **Thumbnailator**: SUPPORTED. Pure Java, no native code. MIT module name since 0.4.19.
- **webp4j**: SUPPORTED. Compiled with JDK 21, targets Java 8 bytecode. Bundles native libs for Linux x64, Linux ARM64, macOS x64, macOS ARM64, Windows x64, Windows ARM64. Java 22+ shows informational JNI warning (JEP 472) but functions correctly.

### Native Dependencies

**YES** — webp4j bundles native libwebp 1.6.0 via JNI.

- **Bundled in JAR**: `native/libwebp4j-linux-x64.so`, `native/libwebp4j-linux-aarch64.so`, etc.
- **No OS package required**: Native libs extracted at runtime from JAR.
- **No Dockerfile changes**: Works on `eclipse-temurin:21-jre` (Debian glibc) without modification.
- **Architecture**: Linux x64 (VPS) and ARM64 (Apple Silicon dev) both supported.
- **Failure mode**: If platform unsupported, `WebPCodec.isAvailable()` returns false. Graceful degradation.

### Docker Impact

**NONE**. Maven dependencies only. No Dockerfile changes required.

### WebP Encode

**YES**. Lossy and lossless. Quality parameter: `float` 0.0–100.0 (higher = better quality, larger file).

### WebP Decode

**YES**. Lossy, lossless, alpha, animation.

### Alpha

**SUPPORTED**. Verified via proof-of-concept:
- Source: `TYPE_INT_ARGB` (2000x1500, semi-transparent)
- Encoded → Decoded: alpha channel preserved, pixel ARGB values verified.
- `BufferedImage.TYPE_INT_ARGB` maintained through pipeline.

### Quality Control

**Mechanism**: `WebPCodec.encodeImage(BufferedImage image, float quality)`
- Quality expressed as `float` 0.0–100.0
- Recommended range: 75.0–85.0 for article covers
- Verified via proof-of-concept: q80 produces good compression/quality balance

### EXIF Orientation

**Via Thumbnailator input decoding**. JPEG bytes are passed directly to Thumbnailator so it can read EXIF orientation before metadata is discarded. A generated orientation-6 JPEG test verifies normalization.

### Metadata Stripping

**YES**. Decode → `BufferedImage` → encode naturally strips:
- EXIF metadata
- GPS data
- Comments
- ICC profiles (not preserved through BufferedImage conversion)

This is the desired behavior for privacy-friendly article covers.

### Resize

**Thumbnailator** provides:
- `Thumbnails.of(image).width(1200).keepAspectRatio(true)` — downscale only, no upscale
- High-quality resampling (Lanczos by default)
- Automatic EXIF orientation handling when decoding JPEG input bytes
- Verified via proof-of-concept: 2000x1500 → 1200x900 (correct aspect ratio)

### Max-Width Hypothesis

**1200px** — sufficient for blog card grid and article hero image. Article covers are not displayed full-width on high-DPI displays.

### Upload-Byte Limit

**5MB** — Spring Boot `spring.servlet.multipart.max-file-size=5MB`. Large enough for high-res photos, small enough to prevent abuse.

### Pixel Safety Limit

**25 megapixels** (5000x5000) — prevents decompression bombs. Reject image if `width * height > 25_000_000` after decode.

### Original Retention

**CONFIRMED NO**. After successful decode/conversion, only the canonical `.webp` is stored.

### Picture Fallback

**CONFIRMED NO**. Single `<img>` tag with `.webp` src. Legacy images remain as-is.

### Existing Legacy Images

**CONFIRMED UNTOUCHED**. No migration. No backfill. Existing `.png`/`.jpg` files continue rendering.

### Template Changes

**NONE**. Existing `<img th:src="${article.coverImage}">` works for both legacy and new articles.

### Architecture

```
ArticleService.create() / update()
    ↓
ArticleImageProcessor.process(MultipartFile)
    ↓
    1. Validate MIME type (PNG/JPEG/WebP only)
    2. Validate size (≤ 5MB)
    3. Decode → BufferedImage (Thumbnailator, ImageIO, or webp4j)
    4. Validate dimensions (≤ 25MP)
    5. Resize if width > 1200px (Thumbnailator)
    6. Encode to WebP (webp4j, quality 0.80)
    ↓
ProcessedImage(byte[] data, int width, int height)
    ↓
FileStorageService.saveArticleWebP(byte[] data)
    → writes to uploads/articles/{uuid}.webp
    → returns URL string
    ↓
Article.coverImage = url
```

### Investigation

Updated artifact: `docs/investigations/automatic-image-optimization-webp.md`

### Production Changes

**NONE** (this is a technical clarification, not implementation)

### Rejected Alternatives

| Library | Reason |
|---------|--------|
| TwelveMonkeys only | No WebP write support (issue #659 open, "sponsor needed") |
| webp-imageio (sejda-pdf) | Unmaintained since Aug 2021 (5 years) |
| NightMonkeys | Requires Java 22+ (Foreign Linker API), incompatible with Java 21 |
| JDeli | Commercial license, not suitable for open-source portfolio |
| cwebp shell command | Requires OS package in Dockerfile, breaks "Maven dependency only" constraint |
| Java 2D AffineTransformOp | Lower quality resize, no EXIF handling, manual interpolation config |

### Implementation Implications

1. **Two new Maven dependencies** (compile scope): `thumbnailator`, `webp4j-core`
2. **Two new types**: `ArticleImageProcessor` (validation + decode + resize + encode) and `ProcessedImage` (result record)
3. **Modified classes**: `ArticleService` (call processor before storage), `FileStorageService` (add `saveArticleWebP` for `uploads/articles/`)
4. **Modified config**: `application.properties` (add `spring.servlet.multipart.max-file-size=5MB`)
5. **No public display template changes**: `<img>` tags work as-is; admin file inputs gain format hints
6. **No database migration**: `Article.coverImage` stores URL string (extension changes from `.png` to `.webp`)

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
