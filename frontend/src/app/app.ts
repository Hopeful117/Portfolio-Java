import { Component, HostListener } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected isAdminRoute = false;
  protected readonly navigation = [
    { path: '/', label: 'Accueil' },
    { path: '/projects', label: 'Projets' },
    { path: '/blog', label: 'Articles' },
    { path: '/skills', label: 'Compétences' },
    { path: '/parcours', label: 'Parcours' },
    { path: '/contact', label: 'Contact' },
  ];
  protected menuOpen = false;

  constructor(router: Router) {
    this.isAdminRoute = router.url.startsWith('/admin');
    router.events.pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd)).subscribe((event) => {
      this.isAdminRoute = event.urlAfterRedirects.startsWith('/admin');
      this.closeMenu();
    });
  }

  protected toggleMenu(): void { this.menuOpen = !this.menuOpen; }
  protected closeMenu(): void { this.menuOpen = false; }

  @HostListener('document:keydown.escape')
  protected onEscape(): void { this.closeMenu(); }

  @HostListener('window:resize')
  protected onResize(): void { if (window.innerWidth > 720) this.closeMenu(); }
}
