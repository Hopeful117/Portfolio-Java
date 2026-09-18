import { Routes } from '@angular/router';

export const routes: Routes = [
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
    path: 'skills',
    loadComponent: () => import('./pages/static.page').then((m) => m.StaticPage),
    data: { heading: 'Compétences', intro: 'Les outils, techniques et pratiques qui structurent le travail.' },
    title: 'Compétences | Portfolio 2.0',
  },
  {
    path: 'journey',
    loadComponent: () => import('./pages/static.page').then((m) => m.StaticPage),
    data: { heading: 'Parcours', intro: 'Les expériences et décisions qui ont façonné ce portfolio.' },
    title: 'Parcours | Portfolio 2.0',
  },
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
    loadComponent: () => import('./pages/static.page').then((m) => m.StaticPage),
    data: { heading: 'Contact', intro: 'Échangeons à propos du logiciel et du web.' },
    title: 'Contact | Portfolio 2.0',
  },
  { path: '**', redirectTo: '' },
];
