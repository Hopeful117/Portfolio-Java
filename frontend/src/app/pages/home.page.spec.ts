import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { PublicApiService } from '../core/public-api.service';
import { HomePage } from './home.page';

describe('HomePage presentation', () => {
  it('renders the real French presentation instead of concept copy', async () => {
    await TestBed.configureTestingModule({
      imports: [HomePage],
      providers: [
        provideRouter([]),
        {
          provide: PublicApiService,
          useValue: { getProjects: () => of([]), getArticles: () => of([]) },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(HomePage);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Je conçois et développe des systèmes logiciels intelligents');
    expect(text).toContain('Developer OS et Trading OS');
    expect(text).toContain('règles métier, des contrôles et des responsabilités clairement définis');
    expect(text).not.toContain('Concept de présentation');
    expect(text).not.toContain('prototype visuel');
  });
});
