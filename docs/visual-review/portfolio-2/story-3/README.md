# Story 3 Visual Review

## Goals

Validate a bounded HopeCodeSec visual foundation without starting Story 4: graphite/slate surfaces, off-white text, restrained blue action color, evidence-oriented composition, accessible navigation, and a serious technical reading surface.

## Screenshots

- [Home desktop, 1440x1000](home-desktop-1440x1000.png)
- [Projects desktop, 1440x1000](projects-desktop-1440x1000.png)
- [Article desktop, 1440x1000](article-desktop-1440x1000.png)
- [Home mobile, 390x844](home-mobile-390x844.png)
- [Projects mobile, 390x844](projects-mobile-390x844.png)
- [Article mobile, 390x844](article-mobile-390x844.png)

## Primitives And Tokens

The implementation keeps shared behavior in the existing application shell and page components rather than adding microcomponents. Global tokens in `frontend/src/styles.css` cover color, surfaces, text, borders, accent, type, spacing, radius, shadow, layout, and transition. The shell provides active links, a semantic menu button, focus-visible states, Escape/resize-safe closing, and reduced-motion rules. Page-level primitives cover evidence cards, project focus/evidence rows, tags, TOC variants, code, tables, figures, and callouts.

## Decisions

- Preserve the dark technical identity while reducing decorative noise and empty hero space.
- Use Ludovic Brot and Java/backend/cloud/application-security wording already supported by the existing public templates.
- Keep projects and articles sourced from the existing typed public API.
- Treat the ecosystem map as a clearly labeled presentation concept based on validated public architecture context, not Developer OS integration.
- Keep rendered article HTML server-owned and bound with Angular's normal `[innerHTML]` sanitization path.
- Keep existing `/blog/:slug` and project API contracts; no project-detail route was introduced.

## Open Visual Questions

- Should the final brand wordmark remain HopeCodeSec or use a more personal Portfolio label?
- Should project evidence labels become API-backed fields when the public contract grows?
- Is the desktop sticky TOC density appropriate for the eventual article inventory?
- Does this feel recognizably HopeCodeSec and sufficiently modern?
- Does it communicate engineering rather than generic web development?
- Does the homepage reveal meaningful work quickly enough?
- Are flagship projects visually important enough?
- Does the article experience feel suitable for serious technical Deep Dives?
- Is the blue accent restrained enough, and is the information density appropriate?
- Is anything too dashboard-like, too SaaS-like, or too cyberpunk/gimmicky?

## Limitations And Fixture Usage

Spring was not running in the local environment, so screenshots used Playwright request interception against the unchanged Angular browser build. The isolated fixture contained one clearly presented public-platform project and one technical article with rendered HTML, TOC, code, and callout markup. It was used only for review and was not added to the application. No loading or empty-state screenshot was used.

## Human Acceptance

**Pending explicit human visual acceptance.** The prototype is ready for review; Story 4 and broader route redesign remain intentionally out of scope.
