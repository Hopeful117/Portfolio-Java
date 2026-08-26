# Story-001 - Generate Stable Unique Article Slugs on Creation

Status: In progress

## Goal

Generate a deterministic public article slug from the title when an article is created, while preserving that slug for the lifetime of the article.

## Investigation Input

This Story implements the completed investigation at `docs/investigations/automatic-article-slug-generation.md`. Current source must remain authoritative if it differs from that report.

## Acceptance Criteria

- New articles receive a server-generated canonical ASCII slug.
- A submitted slug cannot override create-time generation.
- Collisions select the first available numeric suffix: base, `-2`, `-3`, and so on.
- Collision allocation is bounded and terminal failure is user-friendly.
- Editing title, content, publication state, cover image, or other article fields preserves the persisted slug exactly.
- Create and edit forms contain no editable slug field.
- Blank titles and titles that cannot produce a slug are rejected with form feedback.
- Existing article slugs are neither normalized nor migrated.
- Public routing remains `/blog/{slug}`.
- No unique index is activated without production-data compatibility evidence.
- Reverting the application remains data-compatible with articles created by this Story.

## Scope

- Small deterministic `SlugGenerator` component.
- Create-only generation in `ArticleService`.
- Bounded application-level collision handling.
- Duplicate-key retry compatible with a database index if one already exists operationally.
- Stable update semantics and manual-field removal.
- Focused generator, service, controller/template, routing, and legacy tests.
- Read-only production preflight documentation for a future unique-index maintenance step.

## Non-Scope

- Existing slug normalization or migration.
- Redirects, aliases, canonical tags, sitemap, RSS, OpenGraph, or routing redesign.
- Publication-access defect repair.
- Distributed locking.
- A migration framework or automatic index creation.
- Article DTO/domain redesign.

## Production Constraint

Production MongoDB is not accessible in this session. No claim is made about its documents or indexes, and successful deployment must not depend on them being clean.
