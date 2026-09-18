import { convertToParamMap } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { PublicApiService } from '../core/public-api.service';
import { ArticlePage } from './article.page';

describe('ArticlePage anchors', () => {
  it('restores table-of-contents ids removed by innerHTML sanitization', async () => {
    await TestBed.configureTestingModule({
      imports: [ArticlePage],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(convertToParamMap({ slug: 'anchor-test' })),
            fragment: of(null),
            snapshot: { fragment: null },
          },
        },
        {
          provide: PublicApiService,
          useValue: {
            getArticle: () => of({
              title: 'Anchor test',
              slug: 'anchor-test',
              excerpt: null,
              coverImage: null,
              tags: [],
              createdAt: '2026-01-01T00:00:00Z',
              updatedAt: '2026-01-01T00:00:00Z',
              renderedHtml: '<h2 id="section-serveur">Section serveur</h2>',
              tableOfContents: [{ id: 'section-serveur', label: 'Section serveur', level: 2 }],
            }),
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(ArticlePage);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('#section-serveur')).not.toBeNull();
  });
});
