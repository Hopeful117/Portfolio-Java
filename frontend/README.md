# Portfolio 2.0 frontend

This is the Angular SSR application for the public portfolio and the authenticated administration workspace. Public content is read through `/api/public/**`; the admin workspace uses the session-protected `/api/auth/**` and `/api/admin/**` contracts with Angular-compatible CSRF protection.

## Development

Start Spring on port `8080`, then run the Angular development server:

```bash
npm start
```

The development proxy forwards `/api/**` to `http://localhost:8080`. For an SSR process that needs a different backend origin, set `API_ORIGIN` before starting it.

## Verification

```bash
npm run build
npm test -- --watch=false --browsers=ChromeHeadless
```

## Rendering modes

- `/` and the stable informational pages use prerendering.
- `/projects`, `/blog`, and `/blog/:slug` use server rendering because their content comes from the public Spring API.
- Unknown paths use client rendering and are redirected by the application router.

The browser uses hydration with event replay. Article HTML is supplied by Spring and bound with Angular's ordinary `[innerHTML]` sanitization path; the frontend does not parse Markdown or bypass security trust.
