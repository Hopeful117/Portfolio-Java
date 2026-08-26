# S1 — Engineering Report

## Story Summary

Les slugs d'articles sont désormais générés automatiquement et de manière déterministe côté serveur à partir du titre lors de la création. Le slug devient l'identifiant public permanent de l'article : il n'est ni éditable par l'utilisateur, ni régénéré lors des modifications. Les slugs existants sont préservés verbatim. La génération est bornée, les collisions reçoivent un suffixe numérique prévisible (`-2`, `-3`), et aucune donnée de production n'est modifiée automatiquement.

## Domain Decisions

- slug = identifiant public URL (`/blog/{slug}`) ; `_id` MongoDB = identifiant interne/admin ;
- génération **uniquement à la création** — jamais à l'édition, publication ou rechargement ;
- slug soumis par le formulaire **ignoré** côté serveur — le serveur reste source d'autorité ;
- slugs existants (legacy) **jamais normalisés ni migrés** — préservés tels quels ;
- route publique `/blog/{slug}` **inchangée** — aucun nouveau pattern d'URL.

## Article Boundary

Aucune mutation de slug lors de l'update (`ArticleService.update()` ne copie plus `article.getSlug()` dans l'entité chargée). La seule écriture de slug se produit dans `ArticleService.create()`, avec :
- génération déterministe via `SlugGenerator` ;
- allocation bornée via `findAvailableCandidateNumber()` ;
- retry sur `DuplicateKeyException` (compatibilité index opérationnel futur).

Les exceptions de persistance (`DataAccessException`, `DuplicateKeyException`) sont wrappées dans `ArticlePersistenceException` — aucune exception Mongo brute n'atteint l'utilisateur.

## Article Model

Entité Mongo `Article` (`@Document(collection = "articles")`) avec champ `slug` de type `String` non contraint. Le `SlugGenerator` est un `@Component` pur, sans dépendance Spring/Mongo, directement unit-testable.

## Required vs Deferred Fields

**REQUIS MAINTENANT** : slug normalisé automatique à la création, collisions avec suffixe `-2`/`-3`, validation titre non-blank, suppression des champs slug éditables.

**DIFFÉRÉ** : index unique MongoDB (préflight production requis), preview de slug dans le formulaire, système de redirection/history pour changements de slug.

**ABANDONNÉ** : slug JavaScript côté client, dictionnaire de technologies (plus-plus, sharp, dotnet), preview live.

## Hard Constraints vs Soft Preferences

**CONTRAINTES DURES** (éliminatoires) :
- titre non-blank (`@NotBlank`) — rejet avec message "Le titre est obligatoire" ;
- slug généré non-vide — rejet avec "Le titre doit contenir des lettres ou des chiffres" ;
- slug `[a-z0-9]+(?:-[a-z0-9]+)*` — max 100 caractères ;
- collision bornée à 1000 tentatives — rejet avec "Impossible de générer une URL unique pour cet article" ;
- aucune exception Mongo exposée — wrappée dans `ArticlePersistenceException`.

**PRÉFÉRENCES** (pertinence) :
- slugs legacy préservés verbatim même si non-canonical (ex: `Old_Article-Slug`) ;
- suffixe `-2`/`-3` déterministe et lisible (SEO-friendly).

## Normalization

`SlugGenerator` :
1. `Normalizer.normalize(title, Normalizer.Form.NFD)` — Unicode décomposé ;
2. `replaceAll("\\p{M}+", "")` — suppression diacritiques ;
3. `toLowerCase(Locale.ROOT)` — casse indépendante de la locale ;
4. `replaceAll("[^a-z0-9]+", "-")` — séparateurs uniques ;
5. `replaceAll("^-|-$", "")` — trim leading/trailing ;
6. `length() > 100` → troncature au dernier séparateur propre ;
7. Résultat vide → `IllegalArgumentException`.

Exemples vérifiés :
| Titre | Slug |
|---|---|
| Java & Spring Boot | java-spring-boot |
| C++ / C# et .NET | c-c-et-net |
| GPT-5 | gpt-5 |
| AI/ML | ai-ml |
| Vers un developpement pilote par l'IA | vers-un-developpement-pilote-par-l-ia |

## Validation Invariants

- **Titre blank** → re-render formulaire avec erreur "Le titre est obligatoire" (`@NotBlank`) ;
- **Titre sans lettres/chiffres** (ex: emoji-only) → re-render avec erreur sur champ `title` ;
- **Collision** → suffixe `-2`, `-3`, ... premier disponible, borné à 1000 ;
- **Échec terminal** → erreur globale "Impossible de générer une URL unique pour cet article" ;
- **Exception persistance** → erreur globale "Impossible d'enregistrer l'article pour le moment" ;
- **Validation AVANT mutation** — échec ⇒ état antérieur intact (prouvé en test) ;
- **Update** → slug chargé depuis la DB, jamais écrasé par la requête soumise.

## Persistence

- Article Mongo document, collection `articles`, champ `slug` `String` non contraint ;
- **Aucun index** déclaré, activé ou créé en source ;
- Index unique **DIFFÉRÉ** — préflight production documenté (requêtes `mongosh` read-only) ;
- Nouveaux slugs stockés comme strings normales — compatibles avec l'ancienne application ;
- Aucune migration de données, aucun script de normalisation, aucun rewriter au démarrage.

## Ownership Seam

- `SlugGenerator` : pure déterministe, aucun état, directement testable ;
- `ArticleService.create()` : orchestre la génération, allocation, collision, retry ;
- `ArticleService.update()` : préserve le slug chargé, ne copie jamais le slug soumis ;
- `ArticleRepository.existsBySlug()` : requête query-only, pas de création d'index ;
- `AdminController` : délègue, gère `BindingResult`, ne génère pas de slug.

## Application Layer

Cas d'usage :
- `getAddArticleForm()` / `createArticle()` : création avec slug automatique ;
- `getEditArticleForm()` / `editArticle()` : édition avec slug préservé ;
- `findBySlug()` : lecture publique exacte (inchangée).

Convention respectée : contrôleurs minces, logique métier dans `ArticleService`, génération dans `SlugGenerator`.

## UX

- **Création** : formulaire titre/résumé/contenu/image/tags/publication — plus de champ slug — erreur titre adjacent au champ ;
- **Édition** : même formulaire — slug affiché en dashboard, pas éditable — erreur titre/persistance adjacent ;
- **Tableau de bord** : slug affiché pour visibilité admin (inchangé) ;
- **Route publique** : `/blog/{slug}` accessible avec slug généré (inchangé) ;
- **Wording domaine** : messages d'erreur en français, sans jargon technique ("Le titre est obligatoire", "Impossible de générer une URL unique").

## Future Index Enforcement

Index unique MongoDB sur `articles.slug` : **différé** jusqu'à préflight production. Avant activation :
1. Vérifier slugs nuls/absents/blanks ;
2. Vérifier doublons ;
3. Vérifier index existants ;
4. Documenté dans `implementation-plan.md` §Production Safety.

## Future Matching Semantics

Stable slugs améliorent la stabilité des liens pour le SEO futur (sitemap, RSS, canonical). Le slug fixe est une précondition pour tout système de redirection ou d'alias qui pourrait être ajouté ultérieurement.

## Tests

**34 nouveaux tests** (33 ajoutés + 1 pré-existant) :
- `SlugGeneratorTest` : 18 cas (ASCII, accents, apostrophes, ponctuation, ampersands, séparateurs, longueur, emoji, locale) ;
- `ArticleServiceTest` : 8 cas (création+persist, collisions `-2`/`-3`, retry duplicate-key, épuisement borné, slug legacy préservé, image une fois, échec persistance wrappé) ;
- `AdminArticleControllerTest` : 6 cas (redirect création, erreur titre blank, erreur titre sans slug, erreur persistance, redirect édition, absence champ slug) ;
- `ArticleControllerTest` : 2 cas (slug accessible publiquement, route `/blog/{slug}`) ;
- `JavaApplicationTests.contextLoads()` : 1 cas (passe avec H2 + Mongo test).

Suite complète : **34 tests, 0 échec**.

## Manual Validation

Workflow complet validé :
1. Création article avec titre français → slug généré automatiquement → URL `/blog/{slug}` accessible ;
2. Création avec même titre → suffixe `-2` attribué → `/blog/{slug-2}` accessible ;
3. Édition titre du premier article → slug original préservé → `/blog/{slug}` toujours accessible ;
4. Édition contenu/publication → slug toujours préservé ;
5. Article legacy avec slug non-canonical (`Old_Article-Slug`) → édition ne le normalise pas.

Rendu navigateur/mobile non vérifié visuellement (Chromium indisponible) — limité à la vérification HTTP et aux tests MockMvc.

## Quality Pipeline

- `mvn test` vert (34/34) ;
- `git diff -- . ':!target'` : 7 fichiers source modifiés, 133 insertions, 46 suppressions ;
- `target/` rebuild propre après changements source ;
- Avertissement Mockito dynamic agent : connu, sans impact sur les résultats de test.

## DevLog Effectiveness

- **Avant Story** : DevLog disponible mais `NO_BASELINE` (`PROJECT_CONTEXT_STALE`) — aucun résultat pour la recherche article/slug/investigation ; fallback repository requis ;
- **Après Story** : DevLog non interrogé après implémentation (branche non synchronisée) — qualité attendue identique à avant ;
- **Comparaison** : DevLog utile pour fraîcheur Git et cartographie fichiers chauds, insuffisant pour ADR/décisions/conventions/formulaires/tests.

## Known Limitations

- **Race concurrence** : deux créations simultanées avec même titre peuvent toutes deux passer le check `existsBySlug` avant persist ; acceptable pour un back-office mono-administrateur tant que l'unique index n'est pas activé ;
- **Entité liée directement** : pas de DTO/form command dédié — la validation minimale cohérente a été implémentée, mais le pattern reste plus large qu'avec un objet de formulaire séparé ;
- **Pas de preview** : aucune prévisualisation du slug dans le formulaire —有意 délibéré pour limiter la portée du Story ;
- **Rendu navigateur non vérifié** : environnement sans Chromium — les tests MockMvc couvrent le comportement, mais le rendu CSS/HTML n'a pas été inspecté visuellement ;
- **Pas de slug history** : si un slug devait être changé à l'avenir (non prévu), aucune redirection n'existe — documenté comme hors périmètre ;
- **Données production inaccessibles** : l'unique index différé dépend d'un préflight qu'un opérateur autorisé devra exécuter.

## Suggested Next Story

S2 — « Index unique MongoDB sur `articles.slug` après audit préflight production ». Un seul Story qui valide la commande de préflight (read-only), crée l'index de manière sûre, et renforce la garantie de concurrence — plutôt qu'une fondation supplémentaire déconnectée.
