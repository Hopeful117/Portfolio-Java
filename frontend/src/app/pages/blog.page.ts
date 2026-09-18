import { AsyncPipe, DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, map, of, shareReplay, startWith } from 'rxjs';
import { PublicApiService } from '../core/public-api.service';

@Component({
  standalone: true,
  imports: [AsyncPipe, DatePipe, RouterLink],
  template: `
    <section class="page-heading"><p class="eyebrow">Articles</p><h1>Écrire sur le travail réalisé.</h1><p>Les articles sont rendus et publiés par la couche de contenu publique de Spring.</p></section>
    @if (articles$ | async; as state) {
      @if (state.status === 'loading') { <p class="status">Chargement des articles…</p> }
      @else if (state.status === 'error') { <p class="status">Les articles sont temporairement indisponibles.</p> }
      @else if (state.data.length) {
        <div class="article-list">
          @for (article of state.data; track article.slug) {
            <a class="article-card" [routerLink]="['/blog', article.slug]">
              <div><p class="date">{{ article.createdAt | date: 'mediumDate' }}</p><h2>{{ article.title }}</h2><p>{{ article.excerpt || 'Lire l’article complet.' }}</p></div>
              <span aria-hidden="true">↗</span>
            </a>
          }
        </div>
      } @else {
        <p class="empty">Aucun article publié n’est disponible pour le moment.</p>
      }
    }
  `,
  styles: `
    .page-heading { max-width: 700px; margin-bottom: 2.5rem; }
    .eyebrow, .date { color: var(--accent); font-size: .78rem; font-weight: 800; letter-spacing: .12em; text-transform: uppercase; }
    h1 { margin: .75rem 0; font-size: clamp(2.5rem, 7vw, 4.5rem); line-height: 1; letter-spacing: -.06em; }
    .page-heading p:last-child, .article-card p { color: var(--muted); }
    .article-list { display: grid; gap: 1rem; }
    .article-card { display: flex; justify-content: space-between; gap: 2rem; padding: 1.5rem; color: inherit; border: 1px solid var(--line); text-decoration: none; }
    .article-card:hover { border-color: var(--accent); background: var(--accent-soft); }
    h2 { margin: .35rem 0; font-size: 1.5rem; }
    .article-card p { margin: .35rem 0 0; }
    .article-card > span { color: var(--accent); font-size: 1.5rem; }
  `,
})
export class BlogPage {
  private readonly api = inject(PublicApiService);
  protected readonly articles$ = this.api.getArticles().pipe(
    map((data) => ({ status: 'success' as const, data })),
    startWith({ status: 'loading' as const }),
    catchError(() => of({ status: 'error' as const })),
    shareReplay(1),
  );
}
