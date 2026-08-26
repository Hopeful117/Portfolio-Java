# Story-002 Repository Analysis

## Current Image Lifecycle

### Article Cover Image - Create
1. `GET /admin/articles/add` renders `admin/articles/form-articles` with blank `Article`
2. Form: `<input type="file" name="image">` (no `accept` attribute)
3. `POST /admin/articles` receives `@RequestParam("image") MultipartFile image`
4. `ArticleService.create()` checks `!file.isEmpty()` → `fileStorageService.save(file)`
5. `FileStorageService.save()` generates `UUID + "_" + originalFilename`, writes to `uploads/projects/`, returns `/uploads/projects/{filename}`
6. URL stored in `Article.coverImage` in MongoDB

### Article Cover Image - Edit
1. `POST /admin/articles/edit/{id}` receives new `MultipartFile image`
2. `ArticleService.update()` if `!file.isEmpty()`: saves new file, overwrites `existingArticle.coverImage`
3. **Old file on disk is NEVER deleted** (orphan leak)

### Article Cover Image - Public Display
1. `blog.html:52`: `<img th:src="${article.coverImage}" th:alt="${article.title}">`
2. `article.html:77`: `<img th:src="${article.coverImage}" th:alt="${article.title}">`

### Project Image - Out of Scope
- `ProjectService` uses same `FileStorageService` with same `uploads/projects/` path
- Must NOT be modified except where compilation forces minimal non-behavioral change

## Storage Architecture

- **FileStorageService**: Single `save(MultipartFile)` method, writes to `Paths.get("uploads/projects")`
- **WebConfig**: `registry.addResourceHandler("/uploads/**").addResourceLocations("file:uploads/")`
- **Docker**: `uploads:/app/uploads` named volume
- **Security**: `/uploads/**` allowed by catch-all `anyRequest().permitAll()`

## Key Files to Modify

| File | Change |
|------|--------|
| `pom.xml` | Add thumbnailator + webp4j dependencies |
| `application.properties` | Add `spring.servlet.multipart.max-file-size=5MB` |
| `FileStorageService.java` | Add `saveArticleWebP(byte[])` and `deleteArticleAsset(String)` |
| `ArticleService.java` | Integrate `ArticleImageProcessor` in create/update/delete flows |
| `form-articles.html` | Add `accept` attribute to file input |
| `form-edit-articles.html` | Add `accept` attribute to file input |

## Key Files to Create

| File | Purpose |
|------|---------|
| `ArticleImageProcessor.java` | Validation, decode, resize, WebP encode |
| `ProcessedImage.java` | Output record for processor |

## Test Files to Create/Modify

| File | Purpose |
|------|---------|
| `ArticleImageProcessorTest.java` | Processor unit tests |
| `FileStorageServiceTest.java` | Article storage methods tests |
| `ArticleServiceTest.java` | Update existing tests + add image flow tests |
| `AdminArticleControllerTest.java` | Update existing tests + add image validation tests |

## Existing Test Patterns

- JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`)
- Standalone MockMvc for controller tests
- Mocks for MongoDB (no Testcontainers)
- `MockMultipartFile` for upload simulation
- No `@SpringBootTest` except context-loads smoke test
- Descriptive camelCase test method names
