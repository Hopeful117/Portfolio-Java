# Story-001 Implementation Plan

## Implementation Steps

1. Add a focused `SlugGenerator` that validates title input, removes diacritics, emits lowercase ASCII segments, and enforces a 100-character maximum.
2. Add repository slug-existence support.
3. Generate and allocate a slug only in `ArticleService.create`, ignoring submitted slug values and limiting allocation attempts.
4. Retry duplicate-key create conflicts with the next candidate, while converting terminal or other persistence failures to application-level errors.
5. Preserve the loaded article slug in `ArticleService.update`, including legacy noncanonical values.
6. Add minimal title validation and controller `BindingResult` handling.
7. Remove editable slug controls from create and edit templates and render focused validation/save errors.
8. Add generator, service, controller/template, public-route, and legacy regression tests.
9. Run unit/integration tests, package, local runtime checks where infrastructure permits, diff checks, and a focused self-review.

# Production Safety

## Public URL Compatibility

Existing persisted slugs are never regenerated, normalized, or copied from edit requests. `/blog/{slug}` remains unchanged.

## Data Compatibility

No bulk migration, startup rewrite, corrective script, or legacy validation pass will be added. New articles continue to store slug as a normal string.

## Database Assumptions

Source control proves that the application uses MongoDB and has no managed slug index. Production Mongo documents and indexes are unknown and inaccessible in this session. Existing null, missing, blank, duplicate, unusual slugs, or an operational index are all possible.

## Unique Index

**DEFERRED. Unique database enforcement deferred pending production-data preflight.** Enabling an index during ordinary deployment could fail against unknown legacy data. Application checks provide friendly allocation but retain a concurrent-create race. Bounded duplicate-key retries support an already-present operational unique index and the future managed index without making deployment depend on either.

## Rollback

Application rollback is data-compatible. Newly created canonical slugs are ordinary strings already supported by the old application and remain readable through `/blog/{slug}`. No irreversible transformation or schema change is introduced.

## Deployment Risk

Expected risk: **LOW** after verification. The change is create-only for slug generation, updates explicitly retain loaded slugs, routing and storage type are unchanged, and no migration/index activation occurs. Residual risk is concurrent same-title creation before database uniqueness is established.

## Manual Production Preflight

No production check is required for this application-only deployment because no index is activated. Before a future unique-index maintenance step, an authorized operator must run read-only checks in `mongosh` against the intended database:

```javascript
db.articles.countDocuments({ slug: null })
db.articles.countDocuments({ slug: { $exists: false } })
db.articles.countDocuments({ slug: { $type: "string", $regex: /^\s*$/ } })
db.articles.aggregate([
  { $group: { _id: "$slug", count: { $sum: 1 }, ids: { $push: "$_id" } } },
  { $match: { count: { $gt: 1 } } }
])
db.articles.getIndexes()
```

Any result requires separate human review. These queries do not mutate data and are not executed by this Story.

## Verification Focus

- Generator contract and locale independence.
- Automatic create slug and submitted-slug override prevention.
- `-2`/`-3` first-available collision behavior and bounded exhaustion.
- Stable slug through title/content/publication/image edits.
- Exact preservation of `Old_Article-Slug` or equivalent fixture.
- No editable slug controls.
- Existing redirects and `/blog/{slug}` route.
- No index, migration, callback, or bulk rewrite in the final diff.
