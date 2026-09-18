import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { catchError, map, of, shareReplay, startWith } from 'rxjs';
import { PublicApiService } from '../core/public-api.service';

@Component({
  standalone: true,
  imports: [AsyncPipe],
  template: `
    <section class="page-heading"><p class="eyebrow">Projets / preuves</p><h1>Projets</h1><p>Résumés publics des systèmes, de l’implémentation et des technologies exposées à la frontière de l’API.</p></section>
    @if (projects$ | async; as state) {
      @if (state.status === 'loading') { <p class="status">Chargement des projets…</p> }
      @else if (state.status === 'error') { <p class="status">Les projets sont temporairement indisponibles.</p> }
      @else if (state.data.length) {
        <div class="project-grid">@for (project of state.data; track project.title) {<article class="project-card">@if (project.imageUrl) {<img [src]="project.imageUrl" [alt]="'Illustration du projet ' + project.title" />}<div class="project-content"><p class="project-kicker">RÉSUMÉ PUBLIC DU PROJET</p><h2>{{ project.title }}</h2><p class="purpose">{{ project.description || 'Le résumé publié présente l’objectif et le contexte d’implémentation.' }}</p><div class="focus"><span>Focus d’ingénierie</span><strong>Non exposé par le contrat public</strong></div><div class="evidence"><span>Preuves disponibles</span><span>Technologies{{ project.repositoryUrl ? ' + dépôt' : '' }}</span></div><ul>@for (technology of project.technologies; track technology.name) {<li>{{ technology.name }}</li>}</ul>@if (project.repositoryUrl) {<a class="repository" [href]="project.repositoryUrl" target="_blank" rel="noreferrer">Inspecter le dépôt <span aria-hidden="true">↗</span></a>}</div></article>}</div>
      } @else { <p class="empty">Aucun projet public n’est disponible pour le moment.</p> }
    }
  `,
  styles: `
    .page-heading { max-width: 760px; margin-bottom: 3rem; }
    .eyebrow { color: var(--accent); font-size: .78rem; font-weight: 800; letter-spacing: .12em; text-transform: uppercase; }
    h1 { margin: .75rem 0; font-family: var(--font-display); font-size: clamp(3rem, 7vw, 5.2rem); font-weight: 500; line-height: .95; letter-spacing: -.06em; }
    .page-heading p:last-child, .project-card p, .status { color: var(--muted); }
    .project-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 1rem; }
    .project-card { overflow: hidden; background: var(--color-slate); border: 1px solid var(--line); border-radius: var(--radius-md); }
    .project-card img { display: block; width: 100%; max-height: 210px; object-fit: cover; border-bottom: 1px solid var(--line); }
    .project-content { display: flex; min-height: 330px; flex-direction: column; padding: 1.4rem; }
    h2 { margin: .45rem 0; font-family: var(--font-display); font-size: 2rem; font-weight: 500; }
    .project-kicker, .focus span, .evidence span { color: var(--color-subtle); font: var(--type-xs) var(--font-mono); letter-spacing: .06em; text-transform: uppercase; }
    .purpose { color: var(--muted); }
    .focus, .evidence { display: flex; flex-direction: column; gap: .25rem; padding: .7rem 0; border-top: 1px solid var(--line); }
    .focus strong, .evidence > span:last-child { font-size: .9rem; font-weight: 600; }
    .project-card .repository { margin-top: auto; color: var(--accent); font-weight: 700; text-decoration: none; }
    ul { display: flex; flex-wrap: wrap; gap: .4rem; padding: 0; margin: 1rem 0 0; list-style: none; }
    li { padding: .2rem .55rem; background: var(--accent-soft); font-size: .8rem; }
  `,
})
export class ProjectsPage {
  private readonly api = inject(PublicApiService);
  protected readonly projects$ = this.api.getProjects().pipe(
    map((data) => ({ status: 'success' as const, data })),
    startWith({ status: 'loading' as const }),
    catchError(() => of({ status: 'error' as const })),
    shareReplay(1),
  );
}
