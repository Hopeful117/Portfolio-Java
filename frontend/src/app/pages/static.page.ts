import { AsyncPipe, DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { catchError, map, of, shareReplay, startWith } from 'rxjs';
import { PublicApiService } from '../core/public-api.service';

@Component({
  standalone: true,
  imports: [AsyncPipe, DatePipe],
  template: `
    <section class="page-heading"><p class="eyebrow">Portfolio 2.0</p><h1>{{ heading }}</h1><p>{{ intro }}</p></section>
    @if (isSkills) {
      @if (skills$ | async; as state) {
        @if (state.status === 'loading') { <p class="status">Chargement des compétences…</p> }
        @else if (state.status === 'error') { <p class="status">Les compétences sont temporairement indisponibles.</p> }
        @else { <div class="groups">@for (group of state.data; track group.category) {<section class="group"><h2>{{ group.category }}</h2><div class="skills">@for (skill of group.skills; track skill.name) {<article><strong>{{ skill.name }}</strong><span>{{ skill.skillLevel }}</span></article>}</div></section>}</div> }
      }
    } @else {
      @if (journey$ | async; as state) {
        @if (state.status === 'loading') { <p class="status">Chargement du parcours…</p> }
        @else if (state.status === 'error') { <p class="status">Le parcours est temporairement indisponible.</p> }
        @else { <div class="timeline">@for (entry of state.data; track entry.title + entry.date) {<article class="entry"><time>{{ entry.date | date: 'MM/yyyy' }}</time><div><h2>{{ entry.title }}</h2><p>{{ entry.description }}</p>@if (entry.link) {<a [href]="entry.link" target="_blank" rel="noreferrer">Voir le certificat ↗</a>}</div></article>}</div> }
      }
    }
  `,
  styles: `
    .page-heading { max-width: 760px; margin-bottom: 3rem; }
    .eyebrow { color: var(--accent); font-size: .78rem; font-weight: 800; letter-spacing: .12em; text-transform: uppercase; }
    h1 { margin: .75rem 0; font-family: var(--font-display); font-size: clamp(3rem, 7vw, 5.2rem); font-weight: 500; line-height: .95; letter-spacing: -.06em; }
    .page-heading p:last-child, .status, .entry p { color: var(--muted); }
    .groups, .timeline { display: grid; gap: 1rem; max-width: 850px; }
    .group, .entry { padding: 1.4rem; border: 1px solid var(--line); background: var(--color-slate); }
    h2 { margin: 0 0 1rem; font-family: var(--font-display); font-size: 1.8rem; font-weight: 500; }
    .skills { display: flex; flex-wrap: wrap; gap: .6rem; }
    .skills article { display: grid; gap: .2rem; padding: .7rem .8rem; background: var(--accent-soft); }
    .skills span, .entry time { color: var(--accent); font: var(--type-xs) var(--font-mono); text-transform: uppercase; }
    .entry { display: grid; grid-template-columns: 100px 1fr; gap: 1rem; }
    .entry p { margin: .3rem 0 1rem; }
    .entry a { color: var(--accent); font-weight: 700; text-decoration: none; }
    @media (max-width: 600px) { .entry { grid-template-columns: 1fr; gap: .5rem; } }
  `,
})
export class StaticPage {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(PublicApiService);
  protected readonly heading = this.route.snapshot.data['heading'] as string;
  protected readonly intro = this.route.snapshot.data['intro'] as string;
  protected readonly isSkills = this.route.snapshot.routeConfig?.path === 'skills';
  protected readonly skills$ = this.api.getSkills().pipe(
    map((data) => ({ status: 'success' as const, data })),
    startWith({ status: 'loading' as const }),
    catchError(() => of({ status: 'error' as const })),
    shareReplay(1),
  );
  protected readonly journey$ = this.api.getJourney().pipe(
    map((data) => ({ status: 'success' as const, data })),
    startWith({ status: 'loading' as const }),
    catchError(() => of({ status: 'error' as const })),
    shareReplay(1),
  );
}
