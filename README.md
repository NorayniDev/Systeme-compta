# Système Intelligent de Facturation et de Comptabilité pour les PME

Frontend Angular 20 (Standalone Components + Signals) d'une plateforme SaaS de facturation et de comptabilité, conçu pour être connecté à une API REST Spring Boot.

## Stack technique

Angular 20 · TypeScript · Standalone Components · Angular Signals · RxJS · Angular Router · Reactive Forms · Angular Material (Material Design 3) · PrimeNG · Tailwind CSS · SCSS · ngx-translate (FR/EN) · Chart.js (ng2-charts) · FontAwesome · Angular CDK · ESLint · Prettier · Husky · CommitLint · Jasmine/Karma.

## Démarrage rapide

```bash
npm install
npm start          # ng serve — http://localhost:4200
npm run build       # build de production dans dist/
npm test            # tests unitaires (Karma)
npm run lint         # ESLint
npm run format       # Prettier (écrit)
npm run format:check # Prettier (vérifie)
```

## Architecture

```
src/app/
  core/                  Transverse, chargé une seule fois (singleton)
    authentication/      AuthService (JWT + refresh + RBAC), TokenStorageService
    guards/               authGuard, guestGuard, roleGuard(...), permissionGuard(...)
    interceptors/         auth (attache le JWT + refresh silencieux), error, loading
    services/             ApiService, BaseService<T> (CRUD générique), NotificationService, ThemeService, LoadingService
    models/               IUser, IApiError, IPage<T>, ...
    constants/             API_ENDPOINTS, STORAGE_KEYS, roles/permissions RBAC
    helpers/               date, devise (XOF)
    error-handler/         GlobalErrorHandler
  shared/                Réutilisable, sans logique métier propre
    components/            ConfirmDialog, EmptyState
    directives/             *appHasRole, *appHasPermission
    pipes/                  currencyXof, relativeTime
    layouts/                MainLayout (sidebar + topbar), AuthLayout
    widgets/                KpiCard
    helpers/                export CSV/PDF de tableaux
  features/              Un dossier par domaine métier, 100% lazy-loaded
    auth/                   login, forgot-password
    dashboard/               KPIs, graphiques Chart.js, activité récente
    clients/                 CRUD complet (référence de pattern pour les modules suivants)
    invoices/                 Lignes dynamiques, calcul HT/TVA/TTC, workflow de statut
    quotes/                    Idem + cycle de vie devis (envoi/acceptation/refus/conversion en facture)
    payments/                  Encaissement lié à une facture (recherche async), remboursement
    suppliers/                 CRUD complet, symétrique de clients/
    products/                  Catalogue (SKU, prix, TVA, stock), alerte stock faible
    services/                  Catalogue de prestations (IServiceItem/ServiceItemService — nommage
                                volontairement distinct pour éviter l'ambiguïté avec "service" Angular)
    accounting/                 Journal, Grand livre, Balance, Journal de caisse — lecture seule,
                                écritures en partie double générées à partir des factures/paiements
    users/                      Comptes utilisateurs (IUser/UserAccountService — nommage distinct
                                de la notion d'utilisateur courant gérée par AuthService), rôle,
                                statut, réinitialisation de mot de passe
    roles/                      Matrice rôle→permissions en LECTURE SEULE (pas de CRUD — les
                                rôles sont un enum fixe garanti par le typage, voir plus bas)
    settings/                   Profil utilisateur (infos + mot de passe) + paramètres entreprise
                                (raison sociale, TVA par défaut, préfixes de numérotation — ADMIN)
    reports/                    Ventes par client, créances échues, devis par statut — lecture
                                seule, export CSV/PDF par onglet
    audit/                      Journal d'audit (connexions, créations, modifications,
                                suppressions, validations, exports) — lecture seule
  pages/errors/            Pages 401/403/404/500 (composant unique piloté par les route data)
  core/mocks/              Backend simulé en mémoire (mode démo, voir plus bas)
```

Chaque module `features/*` est indépendant et suit la même structure interne : `models/`, `services/`, `pages/`, `validators/`, `*.routes.ts`.

## Authentification & RBAC

- JWT access token + refresh token, rafraîchissement automatique et silencieux sur 401 (`core/interceptors/auth.interceptor.ts`).
- "Se souvenir de moi" : bascule le stockage entre `sessionStorage` (par défaut) et `localStorage`.
- Rôles : `ADMIN`, `COMPTABLE`, `GESTIONNAIRE`, `CAISSIER`, `AUDITEUR`, `UTILISATEUR` (`core/constants/roles.constant.ts`).
- Permissions granulaires par ressource:action (ex: `invoice:validate`), avec une matrice rôle→permissions par défaut en repli si le backend ne les renvoie pas explicitement.
- Protection des routes via `authGuard`, `roleGuard(...roles)`, `permissionGuard(...permissions)`.
- Protection UI via les directives structurelles `*appHasRole` / `*appHasPermission`.

## Internationalisation

Fichiers de traduction dans `public/i18n/{fr,en}.json`, chargés via `@ngx-translate/http-loader`. Le sélecteur de langue est disponible dans la barre supérieure du layout principal. Toute nouvelle chaîne visible doit passer par une clé i18n (pipe `translate`) plutôt qu'être codée en dur.

## Thème

Thème Material 3 généré via `mat.theme()` (variables CSS `--mat-sys-*`), palette bleu/blanc/gris/vert. Mode sombre piloté par `ThemeService` (signal persisté), qui bascule la classe `.dark-theme` sur `<html>` — cette même classe pilote aussi le `darkModeSelector` de PrimeNG.

## Configuration de l'API

`src/environments/environment.ts` (dev) et `environment.prod.ts` définissent `apiUrl`, consommé par `ApiService`. Les DTO/endpoints REST (`core/constants/api-endpoints.constant.ts`) suivent une convention `/api/v1/...` standard ; à ajuster selon le contrat exposé par le backend Spring Boot une fois disponible.

## Mode démo (sans backend)

`environment.useMockApi` (true en dev) active `core/mocks/mock-api.interceptor.ts`, qui simule le backend Spring Boot en mémoire : login accepté avec n'importe quel email/mot de passe, données factices pour dashboard/clients/factures/devis/paiements (l'encaissement d'une facture met à jour son statut : `PAID` si le montant couvre le total, `PARTIALLY_PAID` sinon). Un badge « Mode démo » s'affiche dans la barre supérieure tant que ce flag est actif. Dès qu'un vrai backend est disponible, repasser `useMockApi` à `false` (ou pointer `apiUrl` dessus) — aucun autre changement de code n'est requis.

## Tests

Tests unitaires pour les services (HttpClientTestingModule), un helper pur (`line-item-calculation.helper.spec.ts`) et un composant (`ClientList`). Lancer avec `npm test`. Les nouveaux modules doivent suivre le même niveau de couverture : service + au moins un composant.

## État du projet

Cahier des charges livré en totalité : socle complet (outillage, architecture, auth, RBAC, layout, thème, i18n, dashboard, mode démo) + treize modules métier entièrement fonctionnels (Clients, Factures, Devis, Paiements, Fournisseurs, Produits, Services, Comptabilité, Utilisateurs, Rôles, Paramètres, Rapports, Audit).

Notes de conception :
- **Comptabilité** : contrairement aux modules CRUD, c'est une vue **en lecture seule** côté frontend — les écritures sont générées côté backend (ou, en mode démo, dérivées des factures/paiements existants dans `core/mocks/mock-data.ts::buildMockJournalEntries`) à partir des opérations métier, en partie double (débit = crédit, vérifiable sur la page Balance).
- **Rôles** : également en lecture seule. `UserRole` est un enum TypeScript fixe, vérifié à la compilation par les guards/directives RBAC (`roleGuard`, `*appHasRole`) — permettre la création de rôles arbitraires casserait cette garantie de typage. La page `/roles` documente donc la matrice rôle→permissions plutôt que de la rendre éditable.
- **Rapports** et **Audit** : lecture seule par nature — des analyses agrégées et une trace d'activité, toutes deux produites par le backend (ou dérivées des données existantes en mode démo), jamais saisies manuellement.
