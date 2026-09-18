# Portfolio Java

Portfolio personnel construit autour d’une application Spring Boot et d’un frontend Angular SSR. L’application expose les contenus publics du portfolio et conserve une interface d’administration pour gérer les projets, articles, compétences et éléments du parcours.

## Stack

- Java 21
- Spring Boot 4
- Spring MVC, Spring Data JPA et Spring Data MongoDB
- Angular 20 avec SSR
- PostgreSQL pour les données relationnelles
- MongoDB pour les articles
- Nginx et Docker Compose pour le routage de production

## Architecture publique

- Angular sert les pages `/`, `/projects`, `/skills`, `/journey`, `/blog` et `/blog/:slug`.
- Spring expose le contrat en lecture seule sous `/api/public/**`.
- Les images de projets et d’articles sont stockées sous `uploads/` et servies sous `/uploads/**`.
- Nginx route `/api/**` et `/uploads/**` vers Spring, et les autres routes vers Angular.

Endpoints publics principaux :

| Endpoint | Contenu |
| --- | --- |
| `GET /api/public/articles` | Liste des articles publiés |
| `GET /api/public/articles/{slug}` | Article publié rendu en HTML |
| `GET /api/public/projects` | Projets et technologies publiques |
| `GET /api/public/skills` | Compétences regroupées par catégorie |
| `GET /api/public/journey` | Éléments du parcours triés |

## Installation

1. Copier et renseigner les variables d’environnement nécessaires dans `.env`.
2. Construire et démarrer les services :

   ```bash
   docker compose up --build
   ```

3. Ouvrir l’application via `http://127.0.0.1:8081`.

Le frontend Angular seul peut être développé depuis `frontend/` avec `npm start`. Dans ce mode, Spring doit être disponible sur le port `8080`.

## Vérification

Backend :

```bash
mvn test
```

Frontend :

```bash
cd frontend
npm run build
npm test -- --watch=false --browsers=ChromeHeadless
```

Smoke test de la topologie Docker :

```bash
./scripts/smoke.sh
```

Pour utiliser une autre URL de base :

```bash
SMOKE_BASE_URL=http://127.0.0.1:8081 ./scripts/smoke.sh
```

## Fonctionnalités

- Consultation publique des projets, articles, compétences et parcours
- Rendu SSR Angular des contenus dynamiques
- Stockage et diffusion des images de projets et couvertures d’articles
- Authentification et autorisation de l’administration
- CRUD des contenus du portfolio
- Interface d’administration basée sur Thymeleaf
