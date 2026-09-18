export interface AdminSession { authenticated: boolean; username: string | null; role: string | null; }
export interface AdminSummary { projects: number; articles: number; technologies: number; skills: number; timeline: number; }
export interface AdminTechnology { id: number; name: string; iconUrl: string | null; }
export interface AdminProject { id: number; title: string; description: string; githubUrl: string; imageUrl: string | null; featured: boolean; technologies: AdminTechnology[]; }
export interface AdminSkill { id: number; name: string; category: string; skillLevel: string; }
export interface AdminTimeline { id: number; title: string; date: string; description: string; link: string | null; displayOrder: number | null; type: string; }
export interface AdminArticle { id: string; title: string; slug: string; excerpt: string | null; content: string | null; coverImage: string | null; tags: string[]; published: boolean; createdAt: string; updatedAt: string; }
