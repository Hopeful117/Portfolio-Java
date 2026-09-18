import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="static-page">
      <p class="eyebrow">Portfolio 2.0</p>
      <h1>{{ heading }}</h1>
      <p>{{ intro }}</p>
      <a routerLink="/" class="text-link">Retour à l’accueil</a>
    </section>
  `,
  styles: `
    .static-page { max-width: 700px; padding: 4rem 0; }
    .eyebrow { color: var(--accent); font-size: .78rem; font-weight: 800; letter-spacing: .12em; text-transform: uppercase; }
    h1 { margin: .75rem 0; font-size: clamp(2.5rem, 7vw, 4.5rem); line-height: 1; letter-spacing: -.06em; }
    p:not(.eyebrow) { color: var(--muted); font-size: 1.15rem; }
    .text-link { color: var(--accent); font-weight: 700; }
  `,
})
export class StaticPage {
  private readonly route = inject(ActivatedRoute);
  protected readonly heading = this.route.snapshot.data['heading'] as string;
  protected readonly intro = this.route.snapshot.data['intro'] as string;
}
