import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, map, of, shareReplay, startWith } from 'rxjs';
import { PublicApiService } from '../core/public-api.service';

@Component({
  standalone: true,
  imports: [AsyncPipe, RouterLink],
  template: `
    <section class="page-heading"><p class="eyebrow">Projets / preuves</p><h1>Projets</h1><p>Des systèmes et expérimentations présentés par leur objectif, leurs preuves publiques et les technologies qui les soutiennent.</p></section>
    @if (projects$ | async; as state) {
      @if (state.status === 'loading') { <p class="status">Chargement des projets…</p> }
      @else if (state.status === 'error') { <p class="status">Les projets sont temporairement indisponibles.</p> }
      @else if (state.data.length) {
        <div class="project-list">@for (project of state.data; track project.title; let index = $index) {
          <article class="project-section" [class.reverse]="index % 2 === 1">
            @if (project.imageUrl) {<div class="project-visual"><img [src]="project.imageUrl" [alt]="'Illustration du projet ' + project.title" /></div>}
            <div class="project-story"><p class="project-kicker">PROJET / {{ (index + 1).toString().padStart(2, '0') }}</p><h2>{{ project.title }}</h2><p class="purpose">{{ project.description || 'Le résumé public présente l’objectif et le contexte d’implémentation.' }}</p><div class="evidence"><span>Preuves disponibles</span><strong>{{ project.technologies.length }} technologies{{ project.repositoryUrl ? ' + dépôt public' : '' }}</strong></div><a class="project-action" [routerLink]="['/projects', project.title]">Découvrir le projet <span aria-hidden="true">↗</span></a></div>
          </article>
        }</div>
      } @else { <p class="empty">Aucun projet public n’est disponible pour le moment.</p> }
    }
  `,
  styles: `
    .page-heading { max-width: 760px; margin-bottom: 3rem; }
    .eyebrow { color: var(--accent); font-size: .78rem; font-weight: 800; letter-spacing: .12em; text-transform: uppercase; }
    h1 { margin: .75rem 0; font-family: var(--font-display); font-size: clamp(3rem, 7vw, 5.2rem); font-weight: 500; line-height: .95; letter-spacing: -.06em; }
    .page-heading p:last-child, .purpose, .status { color: var(--muted); }
    .project-list { display: grid; gap: 1rem; }
    .project-section { display: grid; grid-template-columns: minmax(0, 1.1fr) minmax(280px, .9fr); gap: 4rem; align-items: center; padding: 2.5rem 0; border-top: 1px solid var(--line); }
    .project-section.reverse .project-visual { order: 2; }
    .project-section.reverse .project-story { order: 1; }
    .project-visual { overflow: hidden; background: var(--color-slate); border: 1px solid var(--line); border-radius: var(--radius-md); }
    .project-visual img { display: block; width: 100%; min-height: 330px; max-height: 500px; object-fit: cover; transition: transform var(--transition-fast); }
    .project-section:hover .project-visual img { transform: scale(1.02); }
    .project-story { max-width: 560px; }
    .project-kicker, .evidence span { color: var(--color-subtle); font: var(--type-xs) var(--font-mono); letter-spacing: .06em; text-transform: uppercase; }
    h2 { margin: .7rem 0 1rem; font-family: var(--font-display); font-size: clamp(2.2rem, 5vw, 4rem); font-weight: 500; line-height: 1; letter-spacing: -.05em; }
    .purpose { font-size: 1.05rem; }
    .evidence { display: grid; gap: .25rem; margin: 1.5rem 0; padding-top: .85rem; border-top: 1px solid var(--line); }
    .evidence strong { font-size: .95rem; font-weight: 600; }
    .project-action { color: var(--accent); font-weight: 700; text-decoration: none; }
    .project-action:focus-visible { outline: 3px solid var(--focus); outline-offset: 4px; }
    @media (max-width: 760px) { .project-section { grid-template-columns: 1fr; gap: 1.5rem; padding: 1.5rem 0; } .project-section.reverse .project-visual, .project-section.reverse .project-story { order: initial; } .project-visual img { min-height: 220px; } }
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
