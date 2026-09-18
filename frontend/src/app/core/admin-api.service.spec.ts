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
});
