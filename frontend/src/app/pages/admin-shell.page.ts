import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AdminApiService } from '../core/admin-api.service';

@Component({
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `<div class="admin-shell"><aside><a class="brand" routerLink="/admin" aria-label="Administration de Ludovic Brot"><img class="brand-mark" src="brand/horizon-beam.svg" alt="" aria-hidden="true"><span class="brand-copy"><strong>Ludovic Brot</strong><small>Software &amp; AI Engineer</small></span></a><nav aria-label="Navigation administration">@for (item of navigation; track item.path) {<a [routerLink]="item.path" routerLinkActive="active">{{ item.label }}</a>}</nav><div class="aside-footer"><a routerLink="/">Voir le portfolio ↗</a><button type="button" (click)="logout()">Se déconnecter</button></div></aside><main><header><p class="eyebrow">Atelier privé</p><p>Administration du portfolio</p></header><router-outlet /></main></div>`,
  styles: `
     .admin-shell { display:grid; grid-template-columns:230px minmax(0,1fr); min-height:100vh; } aside { display:flex; flex-direction:column; padding:2rem 1.2rem; background:#0b1423; border-right:1px solid var(--line); } .brand { display:inline-flex; align-items:center; gap:.55rem; padding:.5rem .7rem 2rem; color:var(--color-text); text-decoration:none; } .brand-mark { display:block; width:2rem; height:2rem; } .brand-copy { display:grid; gap:.05rem; } .brand-copy strong { color:var(--accent); font-size:1rem; font-weight:800; } .brand-copy small, .eyebrow { color:var(--color-subtle); font:var(--type-xs) var(--font-mono); text-transform:uppercase; letter-spacing:.02em; } nav { display:grid; gap:.35rem; } nav a, .aside-footer a, button { padding:.7rem; color:var(--muted); border:0; background:none; font:inherit; text-align:left; text-decoration:none; cursor:pointer; border-radius:var(--radius-sm); } nav a:hover, nav a.active, .aside-footer a:hover, button:hover { color:var(--color-text); background:var(--accent-soft); } .aside-footer { display:grid; gap:.3rem; margin-top:auto; } main { min-width:0; padding:2rem clamp(1.2rem,4vw,4rem); } main > header { display:flex; justify-content:space-between; padding-bottom:1.5rem; border-bottom:1px solid var(--line); color:var(--muted); } main > header p { margin:0; } @media (max-width:760px) { .admin-shell { grid-template-columns:1fr; } aside { padding:1rem; } .brand { padding-bottom:1rem; } nav { grid-template-columns:repeat(2,1fr); } .aside-footer { margin-top:1rem; grid-template-columns:1fr 1fr; } main { padding:1.2rem; } }
  `,
})
export class AdminShellPage {
  private readonly api = inject(AdminApiService);
  protected readonly navigation = [{ path: '/admin', label: 'Dashboard' }, { path: '/admin/projects', label: 'Projets' }, { path: '/admin/articles', label: 'Articles' }, { path: '/admin/technologies', label: 'Technologies' }, { path: '/admin/skills', label: 'Compétences' }, { path: '/admin/timeline', label: 'Parcours' }];
  protected logout(): void { this.api.csrf().subscribe(() => this.api.logout().subscribe(() => location.assign('/admin/login'))); }
}
