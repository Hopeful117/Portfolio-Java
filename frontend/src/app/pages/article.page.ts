import { AsyncPipe, DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, map, of, shareReplay, switchMap } from 'rxjs';
import { PublicApiService } from '../core/public-api.service';

@Component({
  standalone: true,
  imports: [AsyncPipe, DatePipe, RouterLink],
  template: `
    @if (article$ | async; as article) {
      @if (article) {
        <article class="article-layout">
          <div class="reading-column">
            <a routerLink="/blog" class="back-link">← Archives des articles</a>
            <header class="article-header"><p class="eyebrow">Note technique / {{ article.createdAt | date: 'mediumDate' }}</p><h1>{{ article.title }}</h1><p class="excerpt">{{ article.excerpt || 'Une note technique issue de la pratique d’ingénierie de Ludovic Brot.' }}</p><div class="meta"><span>Publié le {{ article.createdAt | date: 'mediumDate' }}</span>@if (article.updatedAt !== article.createdAt) {<span>Mis à jour le {{ article.updatedAt | date: 'mediumDate' }}</span>}</div><div class="tags" aria-label="Étiquettes de l’article">@for (tag of article.tags; track tag) {<span>#{{ tag }}</span>}</div></header>
            @if (article.coverImage) {<figure class="cover"><img [src]="article.coverImage" [alt]="article.title + ' — illustration'" /></figure>}
            @if (article.tableOfContents.length) {
              <details class="toc mobile-toc"><summary>Dans cet article</summary><nav aria-label="Sommaire de l’article">@for (entry of article.tableOfContents; track entry.id) { <a [class.level-3]="entry.level === 3" [href]="'#' + entry.id">{{ entry.label }}</a> }</nav></details>
            }
            <div class="article-body" [innerHTML]="article.renderedHtml"></div>
          </div>
          @if (article.tableOfContents.length) {<aside class="toc-sidebar"><nav class="toc" aria-label="Sommaire de l’article"><strong>Dans cet article</strong>@for (entry of article.tableOfContents; track entry.id) { <a [class.level-3]="entry.level === 3" [href]="'#' + entry.id">{{ entry.label }}</a> }</nav></aside>}
        </article>
      } @else { <section class="status"><h1>Article introuvable</h1><a routerLink="/blog">Retour aux articles</a></section> }
    } @else { <p class="status">Chargement de l’article…</p> }
  `,
  styles: `
    .article-layout { display: grid; grid-template-columns: minmax(0, var(--layout-reading)) 220px; justify-content: center; gap: 4rem; align-items: start; }
    .reading-column { min-width: 0; }
    .back-link, .toc a, .status a { color: var(--accent); font-weight: 700; text-decoration: none; }
    .article-header { padding: 3rem 0 2rem; border-bottom: 1px solid var(--line); }
    .eyebrow { color: var(--accent); font-size: .78rem; font-weight: 800; letter-spacing: .12em; text-transform: uppercase; }
    h1 { margin: .8rem 0; font-family: var(--font-display); font-size: clamp(2.6rem, 6vw, 4.8rem); font-weight: 500; line-height: .98; letter-spacing: -.06em; }
    .excerpt { color: var(--muted); font-size: 1.15rem; }
    .meta, .tags { display: flex; flex-wrap: wrap; gap: .7rem 1rem; color: var(--color-subtle); font: var(--type-xs) var(--font-mono); }
    .tags { margin-top: 1rem; color: var(--accent); }
    .cover { margin: 2rem 0 0; }
    .cover img { display: block; width: 100%; max-height: 430px; object-fit: cover; border: 1px solid var(--line); border-radius: var(--radius-md); }
    .toc-sidebar { position: sticky; top: 1.5rem; }
    .toc { display: grid; gap: .55rem; padding: 1rem 0; border-top: 2px solid var(--accent); }
    .toc strong { margin-bottom: .4rem; font-size: .8rem; }
    .toc a { color: var(--muted); font-size: .85rem; line-height: 1.35; }
    .toc a:hover { color: var(--accent); }
    .toc .level-3 { padding-left: .8rem; }
    .mobile-toc { display: none; }
    .article-body { padding-top: 2rem; font-size: 1.08rem; }
    .article-body :where(h1, h2, h3, h4) { margin-top: 2.8rem; line-height: 1.15; scroll-margin-top: 1.5rem; }
    .article-body :where(h2) { font-family: var(--font-display); font-size: 2rem; font-weight: 500; }
    .article-body :where(h3) { font-size: 1.35rem; }
    .article-body :where(a) { color: var(--accent); }
    .article-body :where(img) { max-width: 100%; height: auto; border-radius: var(--radius-sm); }
    .article-body :where(figure) { margin: 2rem 0; }
    .article-body :where(figcaption) { color: var(--color-subtle); font-size: .85rem; text-align: center; }
    .article-body :where(pre) { overflow-x: auto; max-width: 100%; padding: 1.2rem; color: #dce8f6; background: #0a0f16; border: 1px solid var(--line); border-radius: var(--radius-sm); font: .88rem/1.6 var(--font-mono); }
    .article-body :where(code) { padding: .1rem .3rem; color: #c9dcf7; background: #192535; border-radius: 3px; font: .88em var(--font-mono); }
    .article-body :where(pre code) { padding: 0; background: transparent; }
    .article-body :where(table) { display: block; width: 100%; overflow-x: auto; border-collapse: collapse; }
    .article-body :where(th, td) { min-width: 8rem; padding: .65rem .8rem; border: 1px solid var(--line); text-align: left; }
    .article-body :where(th) { color: var(--ink); background: var(--color-slate); }
    .article-body :where(blockquote, .article-callout) { margin: 2rem 0; padding: 1rem 1.2rem; color: var(--muted); background: var(--color-slate); border-left: 3px solid var(--accent); }
    .article-body :where(.article-callout-label) { display: block; margin-bottom: .25rem; color: var(--accent); font: var(--type-xs) var(--font-mono); letter-spacing: .08em; }
    .status { padding: 4rem 0; color: var(--muted); }
    @media (max-width: 880px) { .article-layout { display: block; max-width: var(--layout-reading); margin-inline: auto; } .toc-sidebar { display: none; } .mobile-toc { display: block; margin: 2rem 0 0; } .mobile-toc nav { display: grid; } }
  `,
})
export class ArticlePage {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(PublicApiService);
  protected readonly article$ = this.route.paramMap.pipe(
    map((params) => params.get('slug') ?? ''),
    switchMap((slug) => this.api.getArticle(slug).pipe(catchError(() => of(null)))),
    shareReplay(1),
  );
}
