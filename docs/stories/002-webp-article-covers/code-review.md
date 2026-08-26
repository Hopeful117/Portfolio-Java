# Story-002 Code Review

## Recovery Findings

### Fixed: article asset deletion targeted the wrong root

`FileStorageService` saved to `uploads/articles/` but deleted relative to `articles/`. Storage now derives project and article roots from one uploads root, resolves deletion against the article root, and accepts only direct UUID `.webp` filenames.

### Fixed: EXIF normalization was not active

The original upload metadata was discarded before Thumbnailator received a `BufferedImage`. JPEG decoding now passes the original bytes through Thumbnailator, and a generated orientation-6 JPEG test verifies a 40x20 source is normalized to 20x40.

### Fixed: storage tests produced false confidence

Tests now inject `@TempDir`, assert stored bytes exist, assert owned deletion removes the file, and assert legacy/traversal targets remain untouched. Test-generated repository WebPs were removed.

### Fixed: incomplete failure cleanup

Create now cleans the new WebP when a duplicate-key retry is followed by a repository lookup failure. Controller create/edit paths map image I/O failures to a French form error.

## Safety Review

- Project uploads still use the unchanged `save(MultipartFile)` behavior.
- No legacy image migration, rename, conversion, or deletion exists.
- Old active covers are deleted only after a successful article save.
- New orphan cleanup does not mask the original persistence error.
- Delete ownership is limited to pipeline-generated UUID WebPs.
- Original uploads are not retained.
- `Article` remains free of codec/filesystem concerns.
- No generic media abstraction, template redesign, or second image field was introduced.
- No debug statements, abandoned code, or secrets were found in the final source diff.

## Test Review

- Processor coverage includes all formats, size/pixel limits, corrupt input, resize, no upscale, alpha, and EXIF orientation.
- Storage coverage performs real isolated filesystem assertions.
- Service coverage includes create/update/delete, legacy preservation, safe cleanup, and persistence failure paths.
- Controller coverage includes validation and processing errors for create/edit.

## Result

APPROVED after recovery corrections. No unresolved correctness finding remains within Story-002 scope.
