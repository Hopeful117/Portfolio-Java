import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AdminApiService } from './admin-api.service';

describe('AdminApiService', () => {
  let service: AdminApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AdminApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('keeps the admin summary behind the admin API boundary', () => {
    service.summary().subscribe();
    const request = http.expectOne('/api/admin/summary');
    expect(request.request.method).toBe('GET');
    request.flush({ projects: 1, articles: 2, technologies: 3, skills: 4, timeline: 5 });
  });

  it('uses explicit REST verbs for destructive project operations', () => {
    service.deleteProject(7).subscribe();
    const request = http.expectOne('/api/admin/projects/7');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('uses JSON for metadata-only article updates', () => {
    service.updateArticle('article-1', { title: 'Updated' }).subscribe();

    const request = http.expectOne('/api/admin/articles/article-1');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ title: 'Updated' });
    request.flush({});
  });

  it('uses multipart data when an article cover is selected', () => {
    const image = new File(['image'], 'cover.png', { type: 'image/png' });
    service.createArticle({ title: 'With cover' }, image).subscribe();

    const request = http.expectOne('/api/admin/articles');
    expect(request.request.method).toBe('POST');
    expect(request.request.body instanceof FormData).toBeTrue();
    expect((request.request.body as FormData).get('image')).toBe(image);
    request.flush({});
  });

  it('uploads an inline article illustration as multipart data', () => {
    const image = new File(['image'], 'diagram.png', { type: 'image/png' });
    service.uploadArticleIllustration(image).subscribe((result) => expect(result.url).toContain('/uploads/articles/illustrations/'));

    const request = http.expectOne('/api/admin/articles/illustrations');
    expect(request.request.method).toBe('POST');
    expect(request.request.body instanceof FormData).toBeTrue();
    expect((request.request.body as FormData).get('image')).toBe(image);
    request.flush({ url: '/uploads/articles/illustrations/abc.webp' });
  });

  it('removes an article cover through its dedicated endpoint', () => {
    service.removeArticleCover('article-1').subscribe();

    const request = http.expectOne('/api/admin/articles/article-1/cover');
    expect(request.request.method).toBe('DELETE');
    request.flush({ coverImage: null });
  });
});
