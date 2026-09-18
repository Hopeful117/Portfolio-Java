import { Component, HostListener } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly navigation = [
    { path: '/', label: 'Accueil' },
    { path: '/projects', label: 'Projets' },
    { path: '/blog', label: 'Articles' },
    { path: '/skills', label: 'Compétences' },
    { path: '/journey', label: 'Parcours' },
    { path: '/contact', label: 'Contact' },
  ];
  protected menuOpen = false;

  protected toggleMenu(): void { this.menuOpen = !this.menuOpen; }
  protected closeMenu(): void { this.menuOpen = false; }

  @HostListener('document:keydown.escape')
  protected onEscape(): void { this.closeMenu(); }

  @HostListener('window:resize')
  protected onResize(): void { if (window.innerWidth > 720) this.closeMenu(); }
}
