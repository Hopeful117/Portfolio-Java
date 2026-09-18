# Portfolio 2 Story 3.5 Implementation Report

## Classification

`PORTFOLIO_2_IDENTITY_LEGACY_VALIDATION_REQUIRES_DECISION`

This is not an approval and legacy validation is not complete. The local audit found one real published article only (`Test article dev`, slug `test-article-dev`) with a tiny, non-representative body, no tags, and no cover. The PostgreSQL project table is empty. The requested three-real-article corpus is unavailable. Rich renderer captures are explicitly isolated fixtures and are not presented as legacy content.

## Requested Sections

### Identity

Public Angular identity is `Ludovic Brot`; public descriptor is `Software & AI Engineer`. Navigation, footer, home copy, fallback article copy, aria label, title, and logo lockup are consistent. No company brand was invented. Historical docs/tests/internal identifiers were not renamed.

### Logo

Implemented Horizon - Beam as maintainable SVG assets: an abstract horizon arc plus vertical beam/point. The full-color asset is used by the primary lockup and favicon. A monochrome-compatible SVG is included for symbol-scale and monochrome review. It has no technology icon, shield, brackets, planet, particles, or glow dependency.

### Typography

Three fair directions are documented and shown on the same representative surface. A is the OpenCode recommendation: system technical sans for display/body, 500/600/700/800 weights, and a dedicated `ui-monospace` role for code/meta. B is a deliberate restrained editorial serif candidate, not inherited Georgia. C is a humanist/technical system candidate. No proprietary or downloaded font asset and no new dependency was introduced.

### Surfaces And Content

Graphite/slate/off-white/restrained blue are retained. Semantic tokens now cover background, surface, elevated surface, primary/secondary/muted text, border/strong border, accent/hover/subtle, focus, code surface, and identity-specific beam effects. Existing shared shell remains in place. Skills, Journey, and Contact remain shell-linked only; they were not redesigned.

### Legacy Renderer

Spring-owned `renderedHtml` and table of contents remain the source of article content. Angular continues to use ordinary `[innerHTML]` sanitization. No Markdown parser and no `bypassSecurityTrustHtml` were added. Existing article primitives are styled for headings, paragraphs, lists, links, code, tables, figures/captions, and the supported callout.

### Evidence

The review folder contains required desktop/mobile captures for home, projects, and the one real local article, plus separate renderer-fixture mobile code/table crops. Projects show the truthful empty API state. The rich renderer fixture is labelled in the review documentation and was not persisted to the API or database.

### Repository Audit And Preserved Decisions

The audit covered the Story 3 Angular shell, routes, global/component CSS, public API models/client, public templates/CSS, uploads, implementation reports, tests, Git history, and local Docker-backed data. Story 3's dark technical surfaces, restrained blue, evidence-first presentation, progressive disclosure, responsive shell, and Spring-owned article rendering were preserved. Skills, Journey, and Contact remain shared-shell routes only.

### Favicon, Typography, And Loading

`frontend/public/brand/horizon-beam.svg` is the color asset, `horizon-beam-mono.svg` is the flat variant, and `frontend/src/index.html` configures the SVG favicon with the existing ICO fallback. The logo board demonstrates full lockup, compact mark, monochrome, and approximate favicon scales. Typography uses only system/openly available fallback stacks; no remote font, proprietary file, font loader, or new dependency was introduced.

### Legacy And Stress Results

The real article route rendered without errors and showed its original title/body, but it has no tags, cover, headings, code, table, figure, or callout. Consequently those primitives were validated only with an isolated renderer fixture. The fixture exposed and validated long-title wrapping, dense prose rhythm, nested headings, lists, links, code overflow, table overflow, figure/caption sizing, and callout treatment at desktop/mobile widths. The real project route honestly rendered the empty API state because no project records exist.

### Responsive, Accessibility, Performance, And Security

The primary lockup collapses safely into the compact mark/name treatment at mobile width; navigation retains Escape behavior, focus states, semantic controls, and touch-sized links. Article layouts use a narrow reading column, responsive TOC, horizontally scrollable tables, and overflow-safe code. The change adds two small SVG assets and no runtime dependency or font payload. Angular continues normal `[innerHTML]` sanitization; any sanitizer warning remains visible and is not suppressed. No trust bypass or Markdown parser was added.

## A-Z Answers

| Key | Answer |
| --- | --- |
| A | **NO** — the public portfolio is not branded HopeCodeSec. |
| B | **Ludovic Brot**. |
| C | **NO** — no replacement company or agency brand was invented. |
| D | **Horizon - Beam**. |
| E | **NO** — it is an internal visual-direction name, not the public brand. |
| F | **NO** — the selected abstract arc/beam concept was retained. |
| G | **YES** — the mark works flat without glow. |
| H | **YES** — a monochrome SVG exists. |
| I | **YES** — symbol-scale/favicon treatment was demonstrated. |
| J | **3** typography directions. |
| K | A uses `ui-sans-serif`, `system-ui`, `Segoe UI` fallbacks for display/body and `ui-monospace` for code/meta. |
| L | B uses the technical sans stack for UI/body and Iowan Old Style/Palatino-style fallbacks for restrained display serif comparison. |
| M | C uses Trebuchet MS/Segoe UI/system humanist fallbacks with the same monospace role. |
| N | OpenCode recommends **A** for compact technical clarity and better dense-content resilience; human selection remains open. |
| O | **YES** — the same representative surface/content was used for A/B/C comparison. |
| P | **YES**, with an explicit limitation: one real runtime article was available; rich structure uses isolated fixtures. |
| Q | **1 real article** was inspected; three were unavailable. |
| R | `Test article dev` / `test-article-dev`; it is tiny and non-representative. |
| S | **YES**, real code was not present in the one article; code was tested with an isolated renderer fixture. |
| T | **YES**, a real table was not present in the one article; table behavior was tested with an isolated renderer fixture. |
| U | **YES**, a real figure was not present in the one article; figure behavior was tested with an isolated renderer fixture. |
| V | **YES** — Spring remains Markdown/rendering authority. |
| W | **NO** — `bypassSecurityTrustHtml()` was not introduced. |
| X | **NO** — no backend/domain contracts changed. |
| Y | **NO** — production deployment/routing was not changed. |
| Z | **NO** — identity/typography are ready for human judgment, but legacy robustness requires more real content. |

## AA-AP Answers

| Key | Answer |
| --- | --- |
| AA | Preserved graphite/slate surfaces, restrained blue, evidence-first composition, responsive shell, and technical reading primitives. |
| AB | Changed public identity to Ludovic Brot, added Horizon - Beam assets, replaced Georgia-style display with candidate A, and added personal descriptor lockup. |
| AC | HopeCodeSec remains only in historical docs, tests, Java/package history, and prior review artifacts; no public Angular identity occurrence remains. |
| AD | A maintainable SVG draws a single abstract horizon arc and vertical beam/point, with color and monochrome assets. |
| AE | A compact favicon treatment is shown at approximately 16/32/48px scales in the logo review board. |
| AF | Monochrome merges the beam and horizon into one off-white mark without relying on accent color. |
| AG | Added semantic background/surface/text/border/accent/focus/code/identity-beam tokens while retaining the existing palette. |
| AH | Georgia was reconsidered because the prototype was too editorial-first and its metrics vary; dense technical content needs a more stable personal engineering voice. |
| AI | A is compact, technical, readable, and robust; weakness: less distinctive across platforms. |
| AJ | B adds publication credibility; weakness: serif metrics vary and can overstate magazine character. |
| AK | C adds warmth and character; weakness: less precise at large headings and less consistently available. |
| AL | The one real `Test article dev` content was the greatest data limitation, not a visual stress case; the isolated long/dense renderer fixture was the greatest visual stress case. |
| AM | The available real article exposed empty metadata/TOC and sparse-content states; fixture validation exposed long-title, dense prose, code, table, figure, and callout behavior. |
| AN | Added personal lockup, semantic fallback copy, and responsive content styles; did not alter the real article. |
| AO | A representative real article corpus and real public project records require future content/API availability, not CSS. |
| AP | Human must decide identity acceptance, Typography A/B/C/revision, and legacy robustness acceptance/revision before generalization. |

## Files Changed

- `frontend/src/index.html`
- `frontend/src/styles.css`
- `frontend/src/app/app.html`
- `frontend/src/app/app.css`
- `frontend/src/app/app.spec.ts`
- `frontend/src/app/pages/home.page.ts`
- `frontend/src/app/pages/article.page.ts`
- `frontend/src/app/pages/projects.page.ts`
- `frontend/src/app/core/public-api.service.ts`
- `frontend/public/brand/horizon-beam.svg`
- `frontend/public/brand/horizon-beam-mono.svg`
- `docs/visual-review/portfolio-2/story-3-5/README.md`
- `docs/visual-review/portfolio-2/story-3-5/logo-review-board.svg`
- `docs/visual-review/portfolio-2/story-3-5/typography-comparison-board.svg`
- `docs/visual-review/portfolio-2/story-3-5/*.png`

## Exact Verification

- `npm run build` in `frontend/`: passed; browser and server bundles generated, 4 static routes prerendered.
- `npm test -- --watch=false --browsers=ChromeHeadless` in `frontend/`: **4 passed, 0 failed**.
- `frontend/src/app/app.spec.ts`: bounded shell/logo/identity behavior test retained and extended.
- SSR/prerender: build output includes `dist/frontend/server/server.mjs` and reports `Prerendered 4 static routes`.
- Visual evidence: Playwright captures at 1440x1000 and 390x844, with separate code/table mobile crops.
- Public API probe: `localhost:8080/api/public/articles` returned the one published `test-article-dev` record; `/api/public/projects` returned an empty list. No production article data was altered.
- No production data was rewritten.

## Remaining HopeCodeSec Occurrences

No public Angular source occurrence remains under `frontend/src`. Historical Story 3 review material, repository history, Java package names, and other internal identifiers retain HopeCodeSec where already present by design. They were not renamed because this story changes public Angular identity only.

## Future Backend Needs

Provide at least three representative real published articles, including dense content and the supported renderer primitives, plus real public project records. Re-run validation against those records before calling the legacy experience accepted.

## Human Review Gate

Please decide explicitly: identity, typography A/B/C/revision, and legacy robustness accept/revision. Until those decisions and representative data exist, this implementation remains a bounded frontend review candidate, not an approved production identity or completed legacy validation.
