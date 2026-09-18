import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, combineLatest, map, of, shareReplay, startWith } from 'rxjs';
import { PublicApiService } from '../core/public-api.service';

@Component({
  standalone: true,
  imports: [AsyncPipe, RouterLink],
  template: `
    @if (project$ | async; as state) {
      @if (state.status === 'loading') { <p class="status">Chargement du projet…</p> }
      @else if (state.status === 'error' || !state.data) { <section class="status"><h1>Projet introuvable</h1><a routerLink="/projects">Retour aux projets</a></section> }
      @else {
        <article class="project-detail">
          <a routerLink="/projects" class="back-link">← Tous les projets</a>
          <header class="project-hero"><div><p class="eyebrow">Projet / résumé public</p><h1>{{ state.data.title }}</h1><p class="lead">{{ state.data.description || 'Le résumé public présente l’objectif et le contexte d’implémentation.' }}</p><div class="metadata"><span>{{ state.data.technologies.length }} technologies exposées</span>@if (state.data.repositoryUrl) {<span>Dépôt public</span>}</div></div>@if (state.data.imageUrl) {<figure><img [src]="state.data.imageUrl" [alt]="'Illustration du projet ' + state.data.title" /></figure>}</header>
          <div class="detail-grid"><section><p class="eyebrow">Ce qui est exposé</p><h2>Preuves disponibles</h2><p>Cette page s’appuie sur le résumé public actuellement exposé par l’API. Elle ne déduit pas de fonctionnalités ou d’architecture absentes de ce contrat.</p></section><aside class="technology-list"><strong>Technologies</strong><ul>@for (technology of state.data.technologies; track technology.name) {<li>{{ technology.name }}</li>}</ul></aside></div>
          @if (state.data.repositoryUrl) {<a class="repository" [href]="state.data.repositoryUrl" target="_blank" rel="noreferrer">Inspecter le dépôt public <span aria-hidden="true">↗</span></a>}
        </article>
      }
    }
  `,
  styles: `
    .project-detail { max-width: 1080px; margin-inline: auto; }
    .back-link, .repository, .status a { color: var(--accent); font-weight: 700; text-decoration: none; }
    .project-hero { display: grid; grid-template-columns: minmax(0, .9fr) minmax(320px, 1.1fr); gap: 4rem; align-items: center; padding: 3rem 0 4rem; }
    .eyebrow { color: var(--accent); font-size: .78rem; font-weight: 800; letter-spacing: .12em; text-transform: uppercase; }
    h1 { margin: .8rem 0 1rem; font-family: var(--font-display); font-size: clamp(3rem, 7vw, 6rem); font-weight: 500; line-height: .95; letter-spacing: -.06em; }
    .lead { color: var(--muted); font-size: 1.15rem; }
    .metadata { display: flex; flex-wrap: wrap; gap: .6rem 1rem; margin-top: 1.5rem; color: var(--color-subtle); font: var(--type-xs) var(--font-mono); text-transform: uppercase; }
    figure { margin: 0; }
    figure img { display: block; width: 100%; max-height: 560px; object-fit: cover; border: 1px solid var(--line); border-radius: var(--radius-md); }
    .detail-grid { display: grid; grid-template-columns: 1fr 280px; gap: 4rem; padding: 2rem 0; border-top: 1px solid var(--line); border-bottom: 1px solid var(--line); }
    h2 { margin: .5rem 0 1rem; font-family: var(--font-display); font-size: clamp(2rem, 4vw, 3rem); font-weight: 500; line-height: 1; }
    .detail-grid section > p:last-child { max-width: 650px; color: var(--muted); }
    .technology-list { align-self: start; }
    .technology-list strong { font: var(--type-xs) var(--font-mono); letter-spacing: .06em; text-transform: uppercase; }
    ul { display: flex; flex-wrap: wrap; gap: .45rem; padding: 0; margin: 1rem 0 0; list-style: none; }
    li { padding: .25rem .55rem; color: var(--muted); background: var(--accent-soft); font-size: .85rem; }
    .repository { display: inline-block; margin-top: 2rem; }
    .status { padding: 4rem 0; color: var(--muted); }
    .status h1 { font-size: clamp(2.5rem, 6vw, 4rem); }
    @media (max-width: 760px) { .project-hero, .detail-grid { grid-template-columns: 1fr; gap: 2rem; } .project-hero { padding: 2rem 0 3rem; } .project-hero figure { order: -1; } }
  `,
})
export class ProjectDetailPage {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(PublicApiService);
  protected readonly project$ = combineLatest({
    title: this.route.paramMap.pipe(map((params) => params.get('title') ?? '')),
    projects: this.api.getProjects(),
  }).pipe(
    map(({ title, projects }) => ({ status: 'success' as const, data: projects.find((project) => project.title === title) ?? null })),
    startWith({ status: 'loading' as const }),
    catchError(() => of({ status: 'error' as const, data: null })),
    shareReplay(1),
  );
}
