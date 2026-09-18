import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PublicApiService } from './public-api.service';

describe('PublicApiService', () => {
  let service: PublicApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(PublicApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('requests published article summaries through the public contract', () => {
    service.getArticles().subscribe();

    const request = http.expectOne('/api/public/articles');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('encodes article slugs and keeps the API boundary typed', () => {
    service.getArticle('a story/with spaces').subscribe();

    const request = http.expectOne('/api/public/articles/a%20story%2Fwith%20spaces');
    expect(request.request.method).toBe('GET');
    request.flush({
      title: 'A story',
      slug: 'a-story',
      excerpt: null,
      coverImage: null,
      tags: [],
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
      renderedHtml: '<p>Content</p>',
      tableOfContents: [],
    });
  });
});
