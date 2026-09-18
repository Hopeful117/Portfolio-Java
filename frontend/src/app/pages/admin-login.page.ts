import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AdminApiService } from '../core/admin-api.service';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `<main class="login-page"><div class="login-intro"><p class="eyebrow">Espace privé</p><h1>Reprendre la main sur le portfolio.</h1><p>Publier, corriger et maintenir les contenus depuis un atelier unique.</p><a routerLink="/">← Retour au portfolio</a></div><form class="login-form" [formGroup]="form" (ngSubmit)="submit()"><p class="eyebrow">Administration</p><h2>Connexion</h2><label for="username">Identifiant</label><input id="username" type="text" formControlName="username" autocomplete="username"><label for="password">Mot de passe</label><input id="password" type="password" formControlName="password" autocomplete="current-password"><p class="error" role="alert">{{ error }}</p><button type="submit" [disabled]="form.invalid || busy">{{ busy ? 'Connexion…' : 'Ouvrir le dashboard' }}</button></form></main>`,
  styles: `
    .login-page { display:grid; grid-template-columns:1.1fr .9fr; gap:clamp(2rem,8vw,8rem); align-items:center; min-height:calc(100vh - 8rem); padding:4rem clamp(1rem,8vw,8rem); box-sizing:border-box; }
    .login-intro { max-width:620px; } .eyebrow { color:var(--accent); font:var(--type-xs) var(--font-mono); letter-spacing:.08em; text-transform:uppercase; }
    h1 { max-width:650px; margin:.8rem 0 1.2rem; font-family:var(--font-display); font-size:clamp(3rem,7vw,6rem); font-weight:500; line-height:.95; letter-spacing:-.06em; } h2 { margin:.6rem 0 2rem; font-family:var(--font-display); font-size:2.5rem; font-weight:500; }
    .login-intro > p:not(.eyebrow) { max-width:450px; color:var(--muted); font-size:1.1rem; } a { color:var(--accent); font-weight:700; text-decoration:none; }
    .login-form { display:grid; gap:.65rem; max-width:430px; padding:2rem; background:var(--color-slate); border:1px solid var(--line); border-radius:var(--radius-md); } label { margin-top:.5rem; font-size:.85rem; font-weight:700; } input { width:100%; box-sizing:border-box; padding:.85rem 1rem; color:var(--color-text); background:var(--color-bg); border:1px solid var(--line); border-radius:var(--radius-sm); font:inherit; } button { margin-top:1rem; padding:.9rem 1rem; color:#07101e; background:var(--accent); border:0; border-radius:var(--radius-sm); font:inherit; font-weight:800; cursor:pointer; } button:disabled { opacity:.55; cursor:not-allowed; } .error { min-height:1.3rem; margin:.5rem 0 0; color:#ff9c9c; }
    @media (max-width:760px) { .login-page { grid-template-columns:1fr; min-height:auto; padding-top:2rem; } .login-form { max-width:none; } }
  `,
})
export class AdminLoginPage {
  private readonly api = inject(AdminApiService); private readonly router = inject(Router); private readonly fb = inject(FormBuilder);
  protected readonly form = this.fb.nonNullable.group({ username: ['', Validators.required], password: ['', Validators.required] });
  protected busy = false; protected error = '';
  protected submit(): void { if (this.form.invalid) return; this.busy = true; this.error = ''; const { username, password } = this.form.getRawValue(); this.api.csrf().subscribe({ next: () => this.api.login(username, password).subscribe({ next: () => this.router.navigateByUrl('/admin'), error: () => this.failed() }), error: () => this.failed() }); }
  private failed(): void { this.busy = false; this.error = 'Identifiant ou mot de passe invalide.'; }
}
