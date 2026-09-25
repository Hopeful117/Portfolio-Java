import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { convertToParamMap } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { TestBed } from '@angular/core/testing';
import { AdminWorkspacePage } from './admin-workspace.page';

describe('AdminWorkspacePage article covers', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminWorkspacePage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: { paramMap: of(convertToParamMap({ section: 'articles' })) },
        },
      ],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function createFixture() {
    const fixture = TestBed.createComponent(AdminWorkspacePage);
    fixture.detectChanges();
    http.expectOne('/api/admin/articles').flush([
      {
        id: 'article-1',
        title: 'Article',
        excerpt: 'Excerpt',
        content: '# Content',
        coverImage: '/uploads/articles/cover.webp',
        tags: [],
        published: false,
      },
    ]);
    fixture.detectChanges();
    return fixture;
  }

  it('exposes the supported picker and previews the current cover during edit', () => {
    const fixture = createFixture();
    const page = fixture.componentInstance as any;

    expect(fixture.nativeElement.querySelector('#articleCover').accept).toContain('image/png');
    page.edit(page.items[0]);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.cover-preview').src).toContain('/uploads/articles/cover.webp');
  });

  it('persists metadata before explicitly removing the current cover', () => {
    const fixture = createFixture();
    const page = fixture.componentInstance as any;
    page.edit(page.items[0]);
    page.removeCover();
    page.save();

    const update = http.expectOne('/api/admin/articles/article-1');
    expect(update.request.method).toBe('PUT');
    update.flush({ coverImage: '/uploads/articles/cover.webp' });

    const removal = http.expectOne('/api/admin/articles/article-1/cover');
    expect(removal.request.method).toBe('DELETE');
    removal.flush({ coverImage: null });

    http.expectOne('/api/admin/articles').flush([]);
    expect(page.removeCoverRequested).toBeFalse();
  });

  it('uploads an illustration and inserts captioned Markdown at the cursor', async () => {
    const fixture = createFixture();
    const page = fixture.componentInstance as any;
    page.edit(page.items[0]);
    page.form.controls.content.setValue('Before\n\nAfter');
    fixture.detectChanges();

    const content = fixture.nativeElement.querySelector('#content') as HTMLTextAreaElement;
    content.focus();
    content.setSelectionRange(8, 8);
    const image = new File(['image'], 'diagram.png', { type: 'image/png' });
    page.selectIllustration({ target: { files: [image] } } as unknown as Event);
    page.form.controls.illustrationAlt.setValue('Architecture]');
    page.form.controls.illustrationCaption.setValue('Vue "globale"');
    page.uploadIllustration();

    const request = http.expectOne('/api/admin/articles/illustrations');
    expect(request.request.method).toBe('POST');
    request.flush({ url: '/uploads/articles/illustrations/abc.webp' });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(page.form.controls.content.value).toBe('Before\n\n![Architecture\\]](/uploads/articles/illustrations/abc.webp "Vue \\"globale\\"")\n\nAfter');
    expect(page.illustrationMessage).toContain('insérée');
  });

  it('surfaces illustration upload errors without changing content', () => {
    const fixture = createFixture();
    const page = fixture.componentInstance as any;
    page.form.controls.content.setValue('Existing content');
    page.selectIllustration({ target: { files: [new File(['image'], 'diagram.png', { type: 'image/png' })] } } as unknown as Event);
    page.form.controls.illustrationAlt.setValue('Diagram');
    page.uploadIllustration();

    const request = http.expectOne('/api/admin/articles/illustrations');
    request.flush({ message: 'Image invalide' }, { status: 400, statusText: 'Bad Request' });

    expect(page.form.controls.content.value).toBe('Existing content');
    expect(page.error).toBe('Image invalide');
  });
});
