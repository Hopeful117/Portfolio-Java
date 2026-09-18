import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AdminArticle, AdminProject, AdminSession, AdminSkill, AdminSummary, AdminTechnology, AdminTimeline } from './admin-api.models';

@Injectable({ providedIn: 'root' })
export class AdminApiService {
  private readonly http = inject(HttpClient);
  csrf() { return this.http.get('/api/auth/csrf'); }
  session(): Observable<AdminSession> { return this.http.get<AdminSession>('/api/auth/session'); }
  login(username: string, password: string) { const body = new URLSearchParams({ username, password }); return this.http.post('/api/auth/login', body.toString(), { headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, responseType: 'text' }); }
  logout() { return this.http.post('/api/auth/logout', null, { responseType: 'text' }); }
  summary() { return this.http.get<AdminSummary>('/api/admin/summary'); }
  projects() { return this.http.get<AdminProject[]>('/api/admin/projects'); }
  createProject(value: object, image?: File | null) { return image ? this.http.post<void>('/api/admin/projects', this.multipart(value, image)) : this.http.post<void>('/api/admin/projects', value); }
  updateProject(id: number, value: object, image?: File | null) { return image ? this.http.put<void>(`/api/admin/projects/${id}`, this.multipart(value, image)) : this.http.put<void>(`/api/admin/projects/${id}`, value); }
  deleteProject(id: number) { return this.http.delete<void>(`/api/admin/projects/${id}`); }
  technologies() { return this.http.get<AdminTechnology[]>('/api/admin/technologies'); }
  createTechnology(value: object) { return this.http.post<AdminTechnology>('/api/admin/technologies', value); }
  updateTechnology(id: number, value: object) { return this.http.put<AdminTechnology>(`/api/admin/technologies/${id}`, value); }
  deleteTechnology(id: number) { return this.http.delete<void>(`/api/admin/technologies/${id}`); }
  skills() { return this.http.get<AdminSkill[]>('/api/admin/skills'); }
  createSkill(value: object) { return this.http.post<AdminSkill>('/api/admin/skills', value); }
  updateSkill(id: number, value: object) { return this.http.put<AdminSkill>(`/api/admin/skills/${id}`, value); }
  deleteSkill(id: number) { return this.http.delete<void>(`/api/admin/skills/${id}`); }
  timeline() { return this.http.get<AdminTimeline[]>('/api/admin/timeline'); }
  createTimeline(value: object) { return this.http.post<AdminTimeline>('/api/admin/timeline', value); }
  updateTimeline(id: number, value: object) { return this.http.put<AdminTimeline>(`/api/admin/timeline/${id}`, value); }
  deleteTimeline(id: number) { return this.http.delete<void>(`/api/admin/timeline/${id}`); }
  articles() { return this.http.get<AdminArticle[]>('/api/admin/articles'); }
  createArticle(value: object, image?: File | null) { return image ? this.http.post<AdminArticle>('/api/admin/articles', this.multipart(value, image)) : this.http.post<AdminArticle>('/api/admin/articles', value); }
  updateArticle(id: string, value: object, image?: File | null) { return image ? this.http.put<AdminArticle>(`/api/admin/articles/${id}`, this.multipart(value, image)) : this.http.put<AdminArticle>(`/api/admin/articles/${id}`, value); }
  deleteArticle(id: string) { return this.http.delete<void>(`/api/admin/articles/${id}`); }
  private multipart(value: object, image: File): FormData { const form = new FormData(); form.append('data', new Blob([JSON.stringify(value)], { type: 'application/json' })); form.append('image', image); return form; }
}
