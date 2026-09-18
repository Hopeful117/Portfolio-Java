import { AsyncPipe, DatePipe, DecimalPipe, SlicePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, combineLatest, map, of, shareReplay, startWith } from 'rxjs';
import { PublicApiService } from '../core/public-api.service';

@Component({
  standalone: true,
  imports: [AsyncPipe, DatePipe, DecimalPipe, RouterLink, SlicePipe],
  template: `
    <section class="hero">
      <div>
        <p class="eyebrow">Ludovic Brot / Software &amp; AI Engineer</p>
        <h1>Je construis des logiciels dont les preuves restent inspectables.</h1>
        <p class="lede">Java, backend, cloud et sécurité applicative, à travers des projets, des API publiques et des articles techniques.</p>
      </div>
      <div class="actions">
        <a class="button" routerLink="/projects">Découvrir mes projets <span aria-hidden="true">→</span></a>
        <a class="text-link" routerLink="/blog">Lire les articles</a>
      </div>
    </section>
    <section class="signal-strip" aria-label="Axes de travail">
      <span>01 / systèmes</span><span>02 / contrats publics</span><span>03 / livraison sécurisée</span>
    </section>
    @if (view$ | async; as view) {
      <section class="section-block">
        <div class="section-heading"><div><p class="eyebrow">Réalisations sélectionnées</p><h2>Des réalisations qui donnent le cap.</h2></div><a routerLink="/projects">Voir tous les projets <span aria-hidden="true">↗</span></a></div>
        @if (view.projects.length) {
          <div class="featured-grid">
            @for (project of view.projects | slice:0:3; track project.title) {
              <article class="featured-card"><p class="card-index">PROJET / {{ $index + 1 | number: '2.0' }}</p><h3>{{ project.title }}</h3><p>{{ project.description || 'Résumé public du projet et contexte d’implémentation.' }}</p><div class="card-footer"><span>{{ project.technologies.length }} technologies</span>@if (project.repositoryUrl) {<a [href]="project.repositoryUrl" target="_blank" rel="noreferrer">Dépôt ↗</a>}</div></article>
            }
          </div>
        } @else { <p class="muted">Les projets apparaîtront ici lorsque l’API publique publiera leurs résumés.</p> }
      </section>
      <section class="ecosystem-panel" aria-labelledby="ecosystem-title"><div><p class="eyebrow">Concept de présentation</p><h2 id="ecosystem-title">Un écosystème réduit et inspectable.</h2><p>Cette composition est un prototype visuel fondé sur le contexte d’architecture publique validé. Elle ne constitue pas une intégration de Developer OS ni une revendication sur des systèmes privés.</p></div><div class="ecosystem-map"><span>Ludovic Brot</span><i aria-hidden="true"></i><div><b>Projets</b><b>Articles</b><b>API publique</b></div></div></section>
      @if (view.articles.length) {<section class="section-block writing-preview"><div class="section-heading"><div><p class="eyebrow">Derniers articles</p><h2>Notes de réalisation.</h2></div><a routerLink="/blog">Voir les articles ↗</a></div><div class="writing-list">@for (article of view.articles | slice:0:2; track article.slug) {<a [routerLink]="['/blog', article.slug]"><span>{{ article.createdAt | date: 'mediumDate' }}</span><strong>{{ article.title }}</strong><em>{{ article.tags.slice(0, 2).join(' / ') }}</em></a>}</div></section>}
    }
  `,
  styles: `
    .hero { display: grid; grid-template-columns: minmax(0, 1.35fr) minmax(220px, .65fr); align-items: end; gap: 3rem; padding: 4rem 0 3rem; }
    .eyebrow { color: var(--accent); font-size: .78rem; font-weight: 800; letter-spacing: .12em; text-transform: uppercase; }
    h1 { max-width: 780px; margin: 1rem 0; font-family: var(--font-display); font-weight: 500; font-size: var(--type-display); line-height: .98; letter-spacing: -.055em; }
    .lede { max-width: 620px; color: var(--muted); font-size: 1.15rem; }
    .actions { display: flex; flex-wrap: wrap; align-items: center; gap: 1.25rem; margin-top: 2rem; }
    .button { padding: .8rem 1.1rem; color: var(--color-graphite); background: var(--accent); border-radius: var(--radius-sm); text-decoration: none; font-weight: 800; }
    .text-link { color: var(--accent); font-weight: 700; text-decoration: none; }
    .signal-strip { display: flex; flex-wrap: wrap; gap: 2rem; padding: 1rem 0; color: var(--color-subtle); border-top: 1px solid var(--line); border-bottom: 1px solid var(--line); font: var(--type-xs) var(--font-mono); text-transform: uppercase; letter-spacing: .08em; }
    .section-block { padding: 5rem 0 1rem; }
    .section-heading { display: flex; justify-content: space-between; align-items: end; gap: 2rem; margin-bottom: 1.5rem; }
    h2 { margin: .45rem 0 0; font-family: var(--font-display); font-size: clamp(1.8rem, 4vw, 3rem); font-weight: 500; line-height: 1.05; }
    .section-heading a, .card-footer a { color: var(--accent); font-weight: 700; text-decoration: none; }
    .featured-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem; }
    .featured-card { display: flex; min-height: 245px; flex-direction: column; padding: 1.35rem; background: var(--color-slate); border: 1px solid var(--line); border-radius: var(--radius-md); transition: transform var(--transition-fast), border-color var(--transition-fast); }
    .featured-card:hover { transform: translateY(-3px); border-color: var(--color-border-strong); }
    .card-index, .card-footer, .writing-list span, .writing-list em { color: var(--color-subtle); font: var(--type-xs) var(--font-mono); }
    h3 { margin: 1.2rem 0 .5rem; font-size: 1.35rem; }
    .featured-card > p:not(.card-index) { color: var(--muted); }
    .card-footer { display: flex; justify-content: space-between; gap: .5rem; align-items: end; margin-top: auto; }
    .ecosystem-panel { display: grid; grid-template-columns: 1fr 1fr; gap: 3rem; align-items: center; margin-top: 5rem; padding: 2rem; background: linear-gradient(120deg, var(--color-slate), #152538); border: 1px solid var(--line); border-radius: var(--radius-lg); box-shadow: var(--shadow-panel); }
    .ecosystem-panel p:not(.eyebrow) { max-width: 540px; color: var(--muted); }
    .ecosystem-map { display: grid; gap: 1rem; justify-items: center; color: var(--accent); font: var(--type-sm) var(--font-mono); }
    .ecosystem-map i { width: 1px; height: 2rem; background: var(--accent); }
    .ecosystem-map div { display: flex; flex-wrap: wrap; justify-content: center; gap: .5rem; }
    .ecosystem-map b { padding: .65rem .8rem; color: var(--ink); background: var(--color-elevated); border: 1px solid var(--line); border-radius: var(--radius-sm); font-weight: 500; }
    .writing-list { border-top: 1px solid var(--line); }
    .writing-list a { display: grid; grid-template-columns: 125px 1fr 140px; gap: 1rem; align-items: center; padding: 1rem 0; color: var(--ink); border-bottom: 1px solid var(--line); text-decoration: none; }
    .writing-list a:hover strong { color: var(--accent); }
    .writing-list em { text-align: right; font-style: normal; }
    .muted { color: var(--muted); }
    @media (max-width: 760px) { .hero, .ecosystem-panel { grid-template-columns: 1fr; } .hero { padding-top: 2rem; } .featured-grid { grid-template-columns: 1fr; } .section-heading { align-items: flex-start; flex-direction: column; } .writing-list a { grid-template-columns: 1fr; gap: .2rem; } .writing-list em { text-align: left; } }
  `,
})
export class HomePage {
  private readonly api = inject(PublicApiService);
  protected readonly view$ = combineLatest({
    projects: this.api.getProjects().pipe(catchError(() => of([]))),
    articles: this.api.getArticles().pipe(catchError(() => of([]))),
  }).pipe(startWith({ projects: [], articles: [] }), shareReplay(1));
}
