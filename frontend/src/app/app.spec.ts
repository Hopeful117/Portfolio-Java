import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  it('renders the public navigation shell', async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();

    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('nav')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.brand img').getAttribute('src')).toBe('brand/horizon-beam.svg');
    expect(fixture.nativeElement.textContent).toContain('Ludovic Brot');
    expect(fixture.nativeElement.textContent).toContain('Software & AI Engineer');
    expect(fixture.nativeElement.textContent).not.toContain('HopeCodeSec');
    expect(fixture.nativeElement.textContent).toContain('Articles');
    expect(fixture.nativeElement.textContent).toContain('Compétences');
    expect(fixture.nativeElement.textContent).toContain('Parcours');
  });

  it('opens the mobile navigation and closes it with Escape', async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();

    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const toggle = fixture.nativeElement.querySelector('.menu-toggle') as HTMLButtonElement;
    const navigation = fixture.nativeElement.querySelector('nav') as HTMLElement;

    toggle.click();
    fixture.detectChanges();
    expect(toggle.getAttribute('aria-expanded')).toBe('true');
    expect(navigation.classList.contains('is-open')).toBeTrue();

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();
    expect(toggle.getAttribute('aria-expanded')).toBe('false');
  });
});
