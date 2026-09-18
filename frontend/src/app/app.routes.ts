import { Routes } from '@angular/router';
import { adminGuard } from './core/admin.guard';

export const routes: Routes = [
  {
    path: 'admin/login',
    loadComponent: () => import('./pages/admin-login.page').then((m) => m.AdminLoginPage),
    title: 'Connexion admin | Portfolio 2.0',
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin-shell.page').then((m) => m.AdminShellPage),
    children: [
      { path: '', loadComponent: () => import('./pages/admin-dashboard.page').then((m) => m.AdminDashboardPage), title: 'Dashboard | Portfolio 2.0' },
      { path: ':section', loadComponent: () => import('./pages/admin-workspace.page').then((m) => m.AdminWorkspacePage) },
    ],
  },
  {
    path: '',
    loadComponent: () => import('./pages/home.page').then((m) => m.HomePage),
    title: 'Portfolio 2.0',
  },
  {
    path: 'projects',
    loadComponent: () => import('./pages/projects.page').then((m) => m.ProjectsPage),
    title: 'Projets | Portfolio 2.0',
  },
  {
    path: 'projects/:title',
    loadComponent: () => import('./pages/project-detail.page').then((m) => m.ProjectDetailPage),
  },
  {
    path: 'skills',
    loadComponent: () => import('./pages/static.page').then((m) => m.StaticPage),
    data: { heading: 'Compétences', intro: 'Les outils, techniques et pratiques qui structurent le travail.' },
    title: 'Compétences | Portfolio 2.0',
  },
  {
    path: 'parcours',
    loadComponent: () => import('./pages/static.page').then((m) => m.StaticPage),
    data: { heading: 'Parcours', intro: 'Les expériences et décisions qui ont façonné ce portfolio.' },
    title: 'Parcours | Portfolio 2.0',
  },
  { path: 'journey', redirectTo: 'parcours', pathMatch: 'full' },
  {
    path: 'blog',
    loadComponent: () => import('./pages/blog.page').then((m) => m.BlogPage),
    title: 'Articles | Portfolio 2.0',
  },
  {
    path: 'blog/:slug',
    loadComponent: () => import('./pages/article.page').then((m) => m.ArticlePage),
  },
  {
    path: 'contact',
    loadComponent: () => import('./pages/contact.page').then((m) => m.ContactPage),
    title: 'Contact | Portfolio 2.0',
  },
  { path: '**', redirectTo: '' },
];
