# Story 3.5 Visual Review

## Identity Decision

Production candidate: **Ludovic Brot** with the public descriptor **Software & AI Engineer**. No company brand is invented. Historical HopeCodeSec identifiers are not renamed. Human approval is still required.

## Logo

Selected review direction: **Horizon - Beam**. It is an abstract horizon arc with a vertical beam/point. It has no technology icons, shield, brackets, planet, particles, or glow dependency.

- [Logo review board](logo-review-board.svg)
- [Full-color SVG](../../../../frontend/public/brand/horizon-beam.svg)
- [Monochrome SVG](../../../../frontend/public/brand/horizon-beam-mono.svg)

The same treatment is used in the primary lockup, compact navigation mark, SVG favicon, and monochrome asset. The favicon route remains `favicon.ico` as a fallback.

## Typography Candidates

- [Typography comparison board](typography-comparison-board.svg)
- **A - recommended for review:** modern technical system sans. `ui-sans-serif`, `system-ui`, `-apple-system`, `BlinkMacSystemFont`, `Segoe UI`; display and body share the family, with weights 500/600/700/800 and the dedicated `ui-monospace` code role. Strength: stable, compact, technical, excellent for dense real articles. Weakness: less distinctive where the platform font is unavailable.
- **B - comparison only:** technical sans body plus `Iowan Old Style`, `Palatino Linotype`, `Book Antiqua`, serif display. Strength: deliberate editorial contrast. Weakness: serif metrics vary substantially and may overstate the editorial voice.
- **C - comparison only:** `Trebuchet MS`, `Segoe UI`, system humanist/technical fallback. Strength: warmer and more recognisable than a neutral system stack. Weakness: less precise at very large headings and less consistently available.

Georgia is not retained by inertia. Production uses A until a human selects A, B, C, or requests a revision.

## Sample Availability

The local audit found exactly one real published article: `test-article-dev` titled `Test article dev`, with a 26-character body, no tags, and no cover. PostgreSQL contains zero project records. The mandatory three-real-article corpus is unavailable. The `legacy-article-*` captures use that real runtime article; rich article and project records in the `renderer-fixture-*` captures are clearly isolated structural fixtures and are not claims about legacy content.

## Stress Cases

The isolated fixture covers a deliberately long title, dense paragraphs, nested headings, lists, code, a horizontally scrollable table, figure/caption, and the supported callout class. The real `test-article-dev` route was rendered from the local Docker-backed public API. Article HTML remains Spring-owned and uses Angular's normal `[innerHTML]` sanitization path; no Markdown parser or trust bypass was added.

## Screenshots

- [Home desktop, 1440x1000](home-desktop-1440x1000.png)
- [Home mobile, 390x844](home-mobile-390x844.png)
- [Projects desktop, 1440x1000](projects-desktop-1440x1000.png)
- [Projects mobile, 390x844](projects-mobile-390x844.png)
- [Legacy article desktop, 1440x1000](legacy-article-desktop-1440x1000.png)
- [Legacy article mobile, 390x844](legacy-article-mobile-390x844.png)
- [Renderer fixture desktop, 1440x1000](renderer-fixture-desktop-1440x1000.png)
- [Renderer fixture mobile, 390x844](renderer-fixture-mobile-390x844.png)
- [Renderer fixture code mobile](renderer-fixture-code-mobile.png)
- [Renderer fixture table mobile](renderer-fixture-table-mobile.png)

The project captures show the honest API empty state. The legacy article captures show the one real local article. Renderer fixture captures are explicitly separated for typography and primitive stress validation.

## Limitations

- A mandatory three-real-article validation cannot be completed with the available local data.
- No real project card exists locally; the empty API state is the truthful production state.
- Screenshots are visual evidence, not human approval or production cutover.
- Backend/API/domain, Spring/Thymeleaf, Docker, nginx, and production routing were intentionally not changed.

## Recommendation

OpenCode recommends identity `Ludovic Brot`, Horizon - Beam, and typography A for human review. This is not human approval. The overall classification is `PORTFOLIO_2_IDENTITY_LEGACY_VALIDATION_REQUIRES_DECISION`.

## Explicit Human Decisions Required

- Identity: accept `Ludovic Brot` / `Software & AI Engineer`, or request revision.
- Typography: accept A, B, C, or request a revision.
- Legacy robustness: accept the bounded evidence, or request another validation pass after three representative real articles exist.
