# Portfolio 2.0 Story 3: Visual System Prototype

Status: Implemented for visual review; human acceptance pending

## Scope

Story 3 upgrades the existing Angular 20 standalone shell without changing Spring, Thymeleaf, public DTOs, API paths, production routing, dependencies, or the Story 2 rendering architecture. The work stops at a reusable visual foundation and representative home, projects, and article compositions. Skills, journey, and contact were not redesigned.

## Audit And Approach

The starting point was a minimal Story 2 shell with inline page styles, light surfaces, wrapped desktop navigation, API-backed projects/articles, and a server-rendered article HTML contract. The implementation preserves the standalone lazy-route structure and typed `PublicApiService`, then moves the visual language toward the repository's documented HopeCodeSec direction: graphite/slate, off-white text, blue action accent, calm technical density, and evidence before decoration.

## Delivered

- Added explicit native CSS custom property tokens for colors, surfaces, text, borders, accent, type, spacing, radius, shadow, layout, and transitions.
- Added responsive navigation with a semantic button, active route state, focus states, Escape closing, resize-safe closing, touch-sized controls, and reduced-motion support.
- Replaced the generic hero with supported identity, focus areas, API-backed project evidence, latest writing, and a clearly labeled presentation-concept ecosystem map.
- Expanded project cards with purpose, an explicit unavailable engineering-focus state, evidence labels, stack names, image support, and repository links when present. No metrics or invented status were added.
- Added article metadata/tags/cover treatment, narrow reading width, sticky desktop TOC, mobile TOC, and styling for server-provided code, tables, figures/captions, blockquotes, and controlled callouts.
- Added a bounded mobile navigation behavior test while preserving the existing shell/API tests.

## Contract And Safety Boundaries

Spring remains responsible for publication, Markdown parsing, heading IDs, TOC derivation, figures, tables, callouts, and HTML trust policy. Angular does not parse Markdown, does not use `bypassSecurityTrustHtml`, and does not add a project-detail route. The ecosystem panel is only a presentation concept; it does not expose Developer OS integration or speculative private-system truth.

## External Inspiration And Durable Patterns

The review considered [GitHub Engineering](https://github.blog/engineering/), [Linear Method](https://linear.app/method), [Vercel Blog](https://vercel.com/blog), and [Increment](https://increment.com/). Durable patterns taken from these references are clear editorial hierarchy, scannable technical summaries, progressive disclosure, restrained information density, and writing that makes implementation context discoverable. Trends deliberately avoided are copied brand language, animated dashboards, fake activity, excessive gradients, and a design system larger than the current content contract.

## Visual Review

Screenshots and the fixture note are in `docs/visual-review/portfolio-2/story-3/README.md`. Since Spring was unavailable locally, screenshots were captured from the built Angular browser output with Playwright request interception for one isolated project/article fixture. No fixture was committed into application code.

## Verification

Run from `frontend/`:

```text
npm test -- --watch=false --browsers=ChromeHeadless
```

Result: **4 specs passed, 0 failed**.

```text
npm run build
```

Result: **production build succeeded; 4 static routes prerendered**.

## Deferred Work

- Human visual acceptance and any final brand/TOC tuning.
- Real public activity, project status, project detail routes, and richer evidence fields.
- SEO metadata, canonical/social metadata, and production frontend cutover.
- Any redesign of skills, journey, contact, or a future Story 4 homepage expansion.

## Design-System Summary

### Tokens and palette

The application-level token set lives in `frontend/src/styles.css`:

- surfaces: graphite `#10151d`, slate `#171f2b`, elevated `#1d2836`;
- text: off-white `#e9eef5`, muted `#aab6c5`, subtle `#718096`;
- borders: `#2d3a4b` and emphasis `#41536a`;
- primary accent: restrained blue `#70a7ff`, with soft selected surface `#1b3554`;
- semantic supporting colors: muted green, amber, and red for future evidence states;
- typography: serif display for editorial hierarchy, system sans for interface text, monospace for metadata/code;
- spacing, radius, panel shadow, layout width, reading width, and transition tokens are explicit custom properties.

### Layout and components

The shell uses a wide content constraint; project/evidence layouts can use the full content width while article pages use a narrower reading column plus a sticky desktop TOC. Navigation is a compact shell rather than a mega-menu. The home page presents identity, focus areas, API-backed project evidence, writing, and a labeled ecosystem presentation concept in progressive disclosure. Projects use an evidence-oriented card with purpose, presentation-only focus wording, available stack/repository signals, and optional real image/link data. The article page uses Spring's existing TOC and rendered HTML with responsive mobile disclosure, code overflow, responsive tables, figures/captions, blockquotes, and existing callout classes.

### Responsive and accessibility choices

Desktop uses a persistent navigation and side TOC; mobile uses a semantic menu button, vertical navigation, a details-based TOC, one-column project cards, and preserved touch-sized controls. Focus-visible outlines, semantic landmarks, active route state, alt text for project/cover images, heading hierarchy, link clarity, and reduced-motion rules are present. No motion is required for comprehension.

## Future API Needs

No new endpoint was introduced. The prototype demonstrates future needs without pretending they exist: optional public project purpose/focus/status/evidence fields, optional project stable public detail identity, curated activity/milestone projections, and richer article reading metadata. These remain future contract decisions and must not be sourced directly from Developer OS or DevLog internals.

## External Research Sources

- GitHub Engineering: https://github.blog/engineering/ — category-led navigation, technical topic grouping, and article cards that expose context before the click.
- Linear Method: https://linear.app/method — a small set of explicit product principles and progressive disclosure rather than decorative interface density.
- Vercel Blog: https://vercel.com/blog — engineering, security, architecture, and field-engineering categories with scannable technical summaries.
- Increment: https://increment.com/ — long-form editorial hierarchy, topic navigation, author/context framing, and readable technical publication structure.

The durable patterns were hierarchy, context, progressive disclosure, and evidence-oriented summaries. Brand-specific illustration systems, trend-driven gradients, animated dashboards, and large decorative hero treatments were rejected.

## Verification And Screenshots

Browser verification used the Angular development build with Playwright request interception for an isolated public project/article fixture because Spring was not running locally. The verified paths were `/`, `/projects`, and `/blog/technical-content-boundary`; desktop and mobile navigation, project content, article header, TOC, code, and callout content rendered without loading/empty screenshots. The screenshots are:

- `docs/visual-review/portfolio-2/story-3/home-desktop-1440x1000.png`
- `docs/visual-review/portfolio-2/story-3/home-mobile-390x844.png`
- `docs/visual-review/portfolio-2/story-3/projects-desktop-1440x1000.png`
- `docs/visual-review/portfolio-2/story-3/projects-mobile-390x844.png`
- `docs/visual-review/portfolio-2/story-3/article-desktop-1440x1000.png`
- `docs/visual-review/portfolio-2/story-3/article-mobile-390x844.png`

The isolated Playwright run reported no browser console errors. Angular emitted its expected sanitization warning for fixture HTML, confirming that the normal sanitizer remained active rather than being bypassed.

## Files Modified Or Added

- `frontend/src/styles.css`
- `frontend/src/app/app.ts`
- `frontend/src/app/app.html`
- `frontend/src/app/app.css`
- `frontend/src/app/app.spec.ts`
- `frontend/src/app/pages/home.page.ts`
- `frontend/src/app/pages/projects.page.ts`
- `frontend/src/app/pages/article.page.ts`
- `frontend/src/app/core/public-api.service.spec.ts`
- `docs/visual-review/portfolio-2/story-3/README.md`
- `docs/visual-review/portfolio-2/story-3/*.png`
- `docs/implementation/portfolio-2-visual-system-prototype.md`

No Spring, Thymeleaf, API, entity, Docker, nginx, or production-routing files were changed for Story 3.

## Acceptance And Mandatory Answers

| ID | Answer |
|---|---|
| A | **NO** — the entire Portfolio was not redesigned. |
| B | **YES** — a representative visual prototype was implemented. |
| C | **YES** — dark graphite/slate HopeCodeSec identity is preserved. |
| D | **YES** — blue remains the restrained primary accent. |
| E | **NO** — Bootstrap was not introduced into Angular. |
| F | **NO** — Angular Material was not introduced. |
| G | **NO** — no major UI kit was introduced. |
| H | **NO** — Tailwind was not introduced. |
| I | **YES** — meaningful focus and work content starts within the first homepage viewport. |
| J | **YES** — the hero is evidence-oriented, not a generic resume hero. |
| K | **YES** — project presentation includes purpose, focus, evidence signals, stack, and repository. |
| L | **NO** — no fake project metrics were introduced. |
| M | **NO** — no real project-detail backend contract was introduced. |
| N | **NO** — no Developer OS integration was introduced. |
| O | **NO** — no autonomous publishing was introduced. |
| P | **NO** — no public chatbot was introduced. |
| Q | **NO** — no Deep Dive aggregate/profile/type was introduced. |
| R | **YES** — `/blog/:slug` demonstrates the technical article experience. |
| S | **NO** — Angular does not reparse Markdown. |
| T | **NO** — `bypassSecurityTrustHtml()` is not used. |
| U | **YES** — desktop screenshots are available. |
| V | **YES** — mobile screenshots are available. |
| W | **NO** — production cutover was not performed. |
| X | **NO** — Docker/nginx production topology was not changed. |
| Y | **YES** — Spring regression tests pass: 89 tests, 0 failures, 0 errors, 0 skipped. |
| Z | **YES** — Angular tests pass: 4 specs, 0 failures; production build succeeds with 4 prerendered routes. |

### AA–AP

- **AA — Core tokens:** graphite/slate/elevated surfaces; ink/muted/subtle text; subtle/emphasis borders; blue accent/soft selection; semantic status colors; display/body/mono type; spacing, radius, shadow, layout, reading, and transition tokens.
- **AB — Palette:** `#10151d`, `#171f2b`, `#1d2836`, `#e9eef5`, `#aab6c5`, `#718096`, `#2d3a4b`, `#41536a`, and restrained blue `#70a7ff` with `#1b3554` soft accent.
- **AC — Typography:** Georgia fallback for editorial display, system sans for UI/body, and system monospace for metadata/code; no external font dependency.
- **AD — Content width:** shared shell/content width `1160px`; wider evidence/project layouts use the application content constraint.
- **AE — Article width:** `720px` reading column plus a `220px` desktop TOC, collapsing to one column on narrower viewports.
- **AF — Flagship differentiation:** selected work gets evidence-first card composition and homepage placement rather than decorative badges or invented ranking.
- **AG — Engineering evidence:** purpose, implementation-focus label, stack signals, repository links, public API framing, and technical article structure make evidence inspectable.
- **AH — Ecosystem structure:** a labeled, static presentation concept connecting HopeCodeSec to Projects, Writing, and Public API; no interactive graph or private-system claim.
- **AI — Future activity:** the composition leaves room for writing/project evidence rows and future activity projections without adding an activity API.
- **AJ — Deep Dive readiness:** narrow reading column, TOC, metadata, code, tables, figures/captions, blockquotes/callouts, and responsive treatment support future long-form articles.
- **AK — Mobile:** menu collapses behind a semantic button; hero becomes one column; projects stack; article TOC becomes a details disclosure; reading width and touch targets remain usable.
- **AL — Accessibility:** landmarks, heading hierarchy, focus-visible states, semantic menu button, active route state, alt text, keyboard Escape handling, readable contrast, and reduced-motion support.
- **AM — Dependencies:** no new frontend dependency; existing Angular 20, Angular SSR, RxJS, and native CSS remain in use.
- **AN — Future API concepts:** project detail identity/status/evidence, curated activity, and richer article reading metadata require future public contract decisions.
- **AO — Future design system candidates:** global token taxonomy, shell/navigation, evidence/project card, status/technical metadata pattern, article header/TOC, and rendered-content primitives.
- **AP — Three key human questions:** Does the identity feel recognizably HopeCodeSec rather than generic SaaS? Is the homepage/project evidence hierarchy strong enough for fast scanning? Does the article surface feel credible for serious technical Deep Dives without becoming dashboard-like?

## Human Decision

The exact required decision is: **ACCEPT VISUAL DIRECTION** or **REQUEST VISUAL REVISION**. This report does not claim approval. The prototype is technically complete and ready for visual judgment; no broader redesign or next Story should begin before that decision.

Classification: `PORTFOLIO_2_VISUAL_PROTOTYPE_READY`

Readiness: `READY_FOR_HUMAN_VISUAL_REVIEW`
