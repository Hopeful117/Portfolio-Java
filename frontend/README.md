# Portfolio 2.0 frontend

This is the standalone Angular public application foundation. It consumes Spring's read-only `/api/public/**` contract and does not replace the existing Thymeleaf routes or admin application.

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
