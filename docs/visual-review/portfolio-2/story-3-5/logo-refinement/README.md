# Story 3.5b — Horizon–Beam Faithful Concept Translation

## Decision Gate

The personal identity, `Ludovic Brot`, descriptor, Horizon–Beam concept, Typography A, dark graphite/slate palette, and existing page layout are locked. The human decision on A/B/C was **REQUEST REFINEMENT** because those candidates optimized the simplified production icon and lost too much of the original concept's light, depth, and horizon character.

The original concept is the visual target. A/B/C remain review history only and are not being iterated.

## Faithful Translation

- Primary: broad luminous horizon, darker receding extremities, thin tapered beam, concentrated emergence point, layered depth, and restrained blue/cyan variation.
- Small: same geometry and light relationship with reduced blur for approximately 24–32px use.
- Favicon: flat optical fallback for approximately 16px use; it does not constrain the primary treatment.

Assets: `frontend/public/brand/review/horizon-beam-faithful-primary.svg`, `horizon-beam-faithful-small.svg`, and `horizon-beam-faithful-favicon.svg`.

Production remains unchanged until human acceptance.

## Evidence

- [Faithful concept comparison board](horizon-beam-faithful-comparison.svg) — original relationship, Primary/Small/Favicon, and the Primary inside the actual header lockup
- [Faithful desktop header](faithful-header-desktop.png)
- [Faithful mobile header](faithful-header-mobile.png)

The desktop and mobile captures use the real Angular header, the same lockup, descriptor, navigation, background, spacing, and Typography A. Only the image source and review-only optical size are changed during capture; no production source is changed for the comparison.

## Size And Fallback Findings

- The primary keeps the original concept's luminous depth for large lockups and desktop header use.
- The small variant keeps a broad horizon and concentrated center while reducing blur that disappears around 24–32px.
- The favicon variant is intentionally flat and prioritizes arc/beam silhouette at 16px.
- All three are optical variants of one identity and can map to the existing monochrome strategy with one off-white solid color. The current favicon remains unchanged.

## Visual Questions

The review board is intentionally organized around the decision questions:

- Does the mark still read as a broad horizon rather than three strokes or a generic icon?
- Does the precise beam emerge from a concentrated central source and continue below the horizon?
- Does the restrained blue variation add depth without becoming neon, cyberpunk, or a planet mark?
- Does the Primary have enough optical presence beside `Ludovic Brot` while remaining subordinate to the name?
- Does the Small and flat Favicon remain recognizably derived from the same Horizon–Beam identity?

## Implementation Complexity

- The primary uses small native gradients, bounded Gaussian blur, layered horizon paths, and a localized emergence glow; no JavaScript, raster asset, canvas, WebGL, font, or dependency was added.
- The small variant removes the broadest blur while preserving the geometry and focal point.
- The favicon is a flat three-path fallback and remains independent from primary identity fidelity.

## Human Selection Required

Select exactly one: **ACCEPT FAITHFUL HORIZON–BEAM** or **REQUEST REFINEMENT**. No asset is promoted automatically. Typography, identity, palette, and layout are not reopened.

## Classification

`PORTFOLIO_2_HORIZON_BEAM_FAITHFUL_TRANSLATION_READY`

`READY_FOR_HUMAN_VISUAL_REVIEW`

`STOP_FOR_HUMAN_REVIEW`
