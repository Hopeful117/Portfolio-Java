import { HttpClient } from '@angular/common/http';
import { inject, Injectable, InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { PublicArticleDetail, PublicArticleSummary, PublicProjectSummary } from './public-api.models';

export const API_ORIGIN = new InjectionToken<string>('API_ORIGIN', {
  providedIn: 'root',
  factory: () => '',
});

@Injectable({ providedIn: 'root' })
export class PublicApiService {
  private readonly http = inject(HttpClient);
  private readonly apiOrigin = inject(API_ORIGIN);

  getArticles(): Observable<PublicArticleSummary[]> {
    return this.http.get<PublicArticleSummary[]>(this.url('/articles'));
  }

  getArticle(slug: string): Observable<PublicArticleDetail> {
    return this.http.get<PublicArticleDetail>(this.url(`/articles/${encodeURIComponent(slug)}`));
  }

  getProjects(): Observable<PublicProjectSummary[]> {
    return this.http.get<PublicProjectSummary[]>(this.url('/projects'));
  }

  private url(path: string): string {
    return `${this.apiOrigin}/api/public${path}`;
  }
}
