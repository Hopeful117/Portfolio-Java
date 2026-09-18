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
           <div class="featured-projects">
             @for (project of view.projects | slice:0:3; track project.title) {
               <article class="featured-project" [class.reverse]="$index % 2 === 1">
                 @if (project.imageUrl) {<div class="project-visual"><img [src]="project.imageUrl" [alt]="'Illustration du projet ' + project.title" /></div>}
                 <div class="project-story"><p class="card-index">PROJET / {{ $index + 1 | number: '2.0' }}</p><h3>{{ project.title }}</h3><p class="project-purpose">{{ project.description || 'Résumé public du projet et contexte d’implémentation.' }}</p><div class="project-evidence"><span>Preuves disponibles</span><strong>{{ project.technologies.length }} technologies{{ project.repositoryUrl ? ' + dépôt public' : '' }}</strong></div><a class="project-action" [routerLink]="['/projects', project.title]">Explorer le projet <span aria-hidden="true">↗</span></a></div>
               </article>
             }
           </div>
        } @else { <p class="muted">Les projets apparaîtront ici lorsque l’API publique publiera leurs résumés.</p> }
      </section>
      <section class="ecosystem-panel" aria-labelledby="ecosystem-title"><div><p class="eyebrow">Présentation</p><h2 id="ecosystem-title">Construire des systèmes logiciels intelligents.</h2><p>Je conçois et développe des systèmes logiciels intelligents, avec une spécialisation en backend Java/Spring et une attention particulière portée à l’architecture, à la fiabilité et à la qualité du code.</p><p>Mon travail associe développement logiciel et intelligence artificielle : j’intègre progressivement l’IA dans des systèmes où elle apporte une réelle valeur, tout en conservant des règles métier, des contrôles et des responsabilités clairement définis.</p><p>Je développe notamment Developer OS et Trading OS, deux projets qui me servent à explorer concrètement les architectures distribuées, les agents IA, l’exploitation du contexte, l’automatisation et la conception de systèmes complexes.</p><p>Je poursuis en parallèle un apprentissage approfondi de Java, Spring, SQL et des systèmes backend, avec une approche fondée sur l’expérimentation, la mesure et la compréhension des mécanismes plutôt que sur l’accumulation de technologies.</p></div><div class="ecosystem-map"><span>Ludovic Brot</span><i aria-hidden="true"></i><div><b>Java / Spring</b><b>Developer OS</b><b>Trading OS</b><b>IA intégrée</b></div></div></section>
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
     .featured-projects { display: grid; gap: 2rem; }
     .featured-project { display: grid; grid-template-columns: minmax(0, 1.05fr) minmax(280px, .95fr); gap: 3rem; align-items: center; padding: 2rem 0; border-top: 1px solid var(--line); }
     .featured-project.reverse .project-visual { order: 2; }
     .featured-project.reverse .project-story { order: 1; }
     .project-visual { overflow: hidden; min-height: 300px; background: var(--color-slate); border: 1px solid var(--line); border-radius: var(--radius-md); }
     .project-visual img { display: block; width: 100%; height: 100%; min-height: 300px; object-fit: cover; }
     .card-index, .project-evidence span, .writing-list span, .writing-list em { color: var(--color-subtle); font: var(--type-xs) var(--font-mono); }
     h3 { margin: 1rem 0 .8rem; font-family: var(--font-display); font-size: clamp(2rem, 4vw, 3.4rem); font-weight: 500; line-height: 1; letter-spacing: -.04em; }
     .project-purpose { color: var(--muted); }
     .project-evidence { display: grid; gap: .25rem; margin: 1.5rem 0; padding-top: .85rem; border-top: 1px solid var(--line); }
     .project-evidence strong { font-size: .95rem; }
     .project-action { color: var(--accent); font-weight: 700; text-decoration: none; }
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
     @media (max-width: 760px) { .hero, .ecosystem-panel { grid-template-columns: 1fr; } .hero { padding-top: 2rem; } .featured-project { grid-template-columns: 1fr; gap: 1.5rem; padding: 1.5rem 0; } .featured-project.reverse .project-visual, .featured-project.reverse .project-story { order: initial; } .project-visual, .project-visual img { min-height: 220px; } .section-heading { align-items: flex-start; flex-direction: column; } .writing-list a { grid-template-columns: 1fr; gap: .2rem; } .writing-list em { text-align: left; } }
  `,
})
export class HomePage {
  private readonly api = inject(PublicApiService);
  protected readonly view$ = combineLatest({
    projects: this.api.getProjects().pipe(catchError(() => of([]))),
    articles: this.api.getArticles().pipe(catchError(() => of([]))),
  }).pipe(startWith({ projects: [], articles: [] }), shareReplay(1));
}
