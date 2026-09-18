# Portfolio 2.0 Angular Application Foundation

Status: Complete for the foundation scope

## Objective

Add a standalone Angular public application that can evolve into the Portfolio 2.0 experience without replacing the current Thymeleaf application. The implementation deliberately stays within the Story 2 foundation boundary: shell, routes, typed public API clients, hybrid rendering configuration, hydration, and basic functional states.

## Delivered

- Added an Angular 20.3 standalone workspace under `frontend/`.
- Enabled strict TypeScript, standalone components, lazy route components, `provideRouter`, `provideHttpClient(withFetch())`, hydration, and event replay.
- Added the public route set:
  - `/`
  - `/projects`
  - `/skills`
  - `/journey`
  - `/blog`
  - `/blog/:slug`
  - `/contact`
- Added a minimal accessible navigation shell, focus-visible styles, page headings, loading/empty/not-found states, and responsive layout primitives.
- Added typed clients and models for:
  - `GET /api/public/articles`
  - `GET /api/public/articles/{slug}`
  - `GET /api/public/projects`
- Added route-level server rendering policy:
  - prerender: home, skills, journey, contact;
  - SSR: projects, blog listing, article detail;
  - client rendering: unmatched paths.
- Added a development proxy from `/api` to `http://localhost:8080`.
- Added SSR API origin support through `API_ORIGIN`, defaulting to `http://localhost:8080` for the standalone Node SSR process.
- Article detail consumes Spring's `renderedHtml` and `tableOfContents`; it does not add a Markdown parser or call `bypassSecurityTrustHtml`.
- Added Angular shell and public API service tests.
- Added frontend build/development instructions and root ignores for generated frontend output.

## Rendering and ownership boundaries

Spring remains responsible for publication filtering, Markdown parsing, heading IDs, table of contents, tables, figures, callouts, and the server-side HTML trust policy. Angular only consumes public DTOs and renders the returned HTML through Angular's standard sanitizing binding.

The existing Thymeleaf routes and `/admin/**` are untouched. No Spring controller, DTO, entity, database schema, CORS policy, Docker runtime, nginx routing, or production cutover was added for this story.

## Deployment note

The Angular workspace currently builds its own browser and Node SSR output. The existing Maven Docker image and nginx configuration still serve Spring/Thymeleaf only. Integrating the Angular SSR runtime or copying its browser output into Spring static resources requires an explicit deployment decision and is intentionally deferred rather than silently changing production routing.

## Verification

Commands run from `frontend/`:

```text
npm run build
npm test -- --watch=false --browsers=ChromeHeadless
```

The production build completed and prerendered four static routes. The Angular test suite completed successfully. The existing Spring suite should be rerun from the repository root after frontend changes; this story does not modify Spring source.

## Known follow-up work

- Decide whether production should use a separate Angular SSR runtime or an integrated Spring/static deployment.
- Add production proxy/routing and same-origin deployment configuration after that decision.
- Extend the public API before implementing homepage/profile/activity/project-detail experiences that are not part of the current contract.
- Perform visual redesign, SEO metadata, canonical URLs, OpenGraph data, sitemap/robots, and deeper accessibility review in the relevant later stories.
