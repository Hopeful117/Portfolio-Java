import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, map, of, shareReplay, startWith } from 'rxjs';
import { AdminApiService } from '../core/admin-api.service';

@Component({
  standalone: true,
  imports: [AsyncPipe, RouterLink],
  template: `@if (summary$ | async; as state) {<section class="heading"><p class="eyebrow">Dashboard</p><h1>Garder le système lisible.</h1><p>Un point de contrôle sur les contenus qui alimentent le portfolio public.</p></section>@if (state.status === 'loading') {<p>Chargement…</p>} @else if (state.status === 'error') {<p class="error">Impossible de charger le résumé.</p>} @else {<div class="metrics">@for (metric of metrics(state.data); track metric.label) {<a [routerLink]="metric.path"><strong>{{ metric.value }}</strong><span>{{ metric.label }}</span></a>}</div><section class="next"><p class="eyebrow">Prochaine action</p><h2>Commencer par les contenus visibles.</h2><p>Les changements enregistrés ici sont immédiatement relus par les APIs publiques.</p><a routerLink="/admin/projects">Gérer les projets <span aria-hidden="true">↗</span></a></section>}}`,
  styles: `
    .heading { max-width:700px; padding:3rem 0 2rem; } .eyebrow { color:var(--accent); font:var(--type-xs) var(--font-mono); letter-spacing:.08em; text-transform:uppercase; } h1 { margin:.7rem 0 1rem; font-family:var(--font-display); font-size:clamp(3rem,6vw,5.4rem); font-weight:500; line-height:.95; letter-spacing:-.06em; } .heading > p:last-child, .next p:not(.eyebrow) { color:var(--muted); } .metrics { display:grid; grid-template-columns:repeat(5,1fr); border-top:1px solid var(--line); border-bottom:1px solid var(--line); } .metrics a { display:grid; gap:.4rem; padding:1.4rem 1rem; color:var(--color-text); text-decoration:none; border-right:1px solid var(--line); } .metrics a:hover { background:var(--accent-soft); } .metrics strong { font-family:var(--font-display); font-size:2.8rem; font-weight:500; } .metrics span { color:var(--muted); font-size:.85rem; } .next { max-width:650px; margin-top:5rem; padding-top:1.5rem; border-top:1px solid var(--line); } h2 { margin:.5rem 0 1rem; font-family:var(--font-display); font-size:2.2rem; font-weight:500; } .next a { color:var(--accent); font-weight:700; text-decoration:none; } .error { color:#ff9c9c; } @media (max-width:760px) { .heading { padding-top:2rem; } .metrics { grid-template-columns:repeat(2,1fr); } .metrics a:nth-child(2n) { border-right:0; } .metrics a:last-child { grid-column:1/-1; } }
  `,
})
export class AdminDashboardPage {
  private readonly api = inject(AdminApiService);
  protected readonly summary$ = this.api.summary().pipe(map((data) => ({ status: 'success' as const, data })), startWith({ status: 'loading' as const }), catchError(() => of({ status: 'error' as const })), shareReplay(1));
  protected metrics(data: { projects: number; articles: number; technologies: number; skills: number; timeline: number }) { return [{ label: 'Projets', value: data.projects, path: '/admin/projects' }, { label: 'Articles', value: data.articles, path: '/admin/articles' }, { label: 'Technologies', value: data.technologies, path: '/admin/technologies' }, { label: 'Compétences', value: data.skills, path: '/admin/skills' }, { label: 'Parcours', value: data.timeline, path: '/admin/timeline' }]; }
}
