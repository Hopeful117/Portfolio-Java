import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  { path: 'admin/login', renderMode: RenderMode.Client },
  { path: 'admin/**', renderMode: RenderMode.Client },
  { path: '', renderMode: RenderMode.Prerender },
  { path: 'projects', renderMode: RenderMode.Server },
  { path: 'projects/:title', renderMode: RenderMode.Server },
  { path: 'skills', renderMode: RenderMode.Prerender },
  { path: 'parcours', renderMode: RenderMode.Prerender },
  { path: 'journey', renderMode: RenderMode.Prerender },
  { path: 'blog', renderMode: RenderMode.Server },
  { path: 'blog/:slug', renderMode: RenderMode.Server },
  { path: 'contact', renderMode: RenderMode.Prerender },
  { path: '**', renderMode: RenderMode.Client },
];
