export interface PublicArticleSummary {
  title: string;
  slug: string;
  excerpt: string | null;
  coverImage: string | null;
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface TableOfContentsEntry {
  id: string;
  label: string;
  level: number;
}

export interface PublicArticleDetail extends PublicArticleSummary {
  renderedHtml: string;
  tableOfContents: TableOfContentsEntry[];
}

export interface PublicTechnology {
  name: string;
  iconUrl: string | null;
}

export interface PublicProjectSummary {
  title: string;
  description: string | null;
  imageUrl: string | null;
  repositoryUrl: string | null;
  technologies: PublicTechnology[];
}
