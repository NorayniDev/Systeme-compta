# Facturation PME — Backend

Backend Spring Boot du Système Intelligent de Facturation et de Comptabilité pour les PME. Sert de contrepartie API REST au frontend Angular 20 (`../` à la racine du dépôt) : mêmes routes, mêmes DTO, mêmes formats de pagination et d'erreur — voir `SPRING_BOOT_BACKEND_PROMPT.md` pour le contrat détaillé.

## Stack

Java 25 · Spring Boot 3.3 · Spring Security 6 (JWT access + refresh token opaque révocable) · Spring Data JPA · PostgreSQL · Flyway · MapStruct · springdoc-openapi (Swagger UI) · JUnit 5 / Mockito / Testcontainers / ArchUnit · Checkstyle · Spotless (google-java-format) · JaCoCo.

## Démarrage rapide (Docker, recommandé)

```bash
docker compose up -d --build
```

Démarre PostgreSQL + le backend (port `8080`, base path `/api/v1`). Flyway applique les migrations et seed un compte de démonstration au premier démarrage :

```
email    : admin@facturation-pme.sn
password : Admin123!
```

Vérification :

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@facturation-pme.sn","password":"Admin123!","rememberMe":false}'
```

Swagger UI : http://localhost:8080/api/v1/swagger-ui.html (profil `dev` uniquement).

## Développement local sans Docker pour le build

Java 25 et Maven ne sont pas requis en local si Docker est disponible : le wrapper (`./mvnw`) télécharge Maven au premier appel, mais nécessite un JDK 25 sur le PATH. En son absence, utiliser l'image Maven officielle :

```bash
docker run --rm -v "$(pwd):/app" -w /app maven:3.9-eclipse-temurin-25 mvn -B verify
```

## Tests

- Unitaires (`*Test`) : services avec Mockito, aucune dépendance Spring.
- Architecture (`LayeringArchitectureTest`, ArchUnit) : empêche les contrôleurs d'accéder directement aux repositories, et le domaine de dépendre de la couche service/web.
- Intégration (`*IT`, suffixe requis par Failsafe) : `AuthAndClientsIT` démarre un vrai PostgreSQL via Testcontainers et exerce login → liste paginée → création → erreurs (401/422/409) sur le contrat HTTP réel. Nécessite que Maven ait accès au démon Docker (montage du socket si Maven tourne lui-même en conteneur) :

```bash
docker run --rm \
  -v "$(pwd):/app" -v "$(pwd)/../.m2:/root/.m2" \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -w /app maven:3.9-eclipse-temurin-25 mvn -B verify
```

Sur Docker Desktop pour Windows, le socket Unix n'est pas toujours exposé côté hôte (accès uniquement via named pipe) : dans ce cas, valider le même scénario manuellement via `docker compose up` + `curl` (voir plus haut) plutôt que via le test automatisé, qui reste correct et s'exécutera normalement en CI (GitHub Actions, GitLab CI, etc. exposent le socket Docker nativement).

`mvn verify` échoue le build si Checkstyle ou Spotless détecte une violation (`mvn spotless:apply` corrige automatiquement le formatage) ou si la couverture JaCoCo descend sous 80 % sur les packages `service`.

## Architecture

Package-by-feature, symétrique de `features/*` côté Angular : `auth/`, `users/`, `clients/`, ... (voir `SPRING_BOOT_BACKEND_PROMPT.md` §2 pour le détail). Chaque module suit `domain/ → repository/ → service/ → web/ (+ dto/, mapper/)`, dépendances à sens unique.

## Sécurité

- JWT d'accès (15 min par défaut) signé HS256, claims `sub/email/role/permissions` — les permissions sérialisent en `"resource:action"` (ex. `"invoice:create"`), jamais le nom de constante Java.
- Refresh token opaque (256 bits), à usage unique (rotation), stocké hashé (SHA-256) en base — jamais en clair, jamais sous forme de JWT.
- `RolePermissions` (backend) est la copie fidèle de `ROLE_PERMISSIONS` (frontend, `core/constants/roles.constant.ts`) : à maintenir synchronisée en cas d'évolution des rôles.
- Mot de passe : BCrypt, coût 12.

## Paiement en ligne (Wave / Orange Money)

`payments/gateway/` définit une abstraction `PaymentGatewayProvider` (`initiateCheckout`, `parseWebhook`) commune à tout fournisseur de paiement en ligne, résolue via `PaymentGatewayRegistry` :

- **Wave** (`payments/gateway/wave/`) : intégration réelle du Wave Checkout API (base URL, endpoint `POST /v1/checkout/sessions`, authentification Bearer et webhooks signés HMAC-SHA256 vérifiés le 2026-07-10 contre docs.wave.com/business). La forme exacte du payload webhook (types d'événements, champs) n'a pas pu être confirmée publiquement — voir le commentaire dans `WaveWebhookPayload` — à valider contre un vrai compte marchand avant mise en production.
- **Orange Money** (`payments/gateway/orangemoney/`) : **non implémenté**. La documentation technique (endpoints, schémas de requête) est derrière un compte développeur sur developer.orange-sonatel.com, non accessible publiquement. `OrangeMoneyProvider` échoue explicitement (503, pas un plantage silencieux) et documente en Javadoc les étapes exactes pour la compléter une fois des identifiants obtenus.

Configuration (`app.payment.wave.*`, variables d'environnement `WAVE_API_KEY`/`WAVE_WEBHOOK_SECRET`) : vide par défaut en dev — `POST /payments/checkout` répond alors `503 PAYMENT_GATEWAY_UNAVAILABLE` avec un message explicite plutôt qu'un 500 générique, un état attendu tant qu'aucun compte marchand réel n'est renseigné.

Le CRUD de paiement manuel (`POST /payments`, `POST /payments/{id}/refund`) reste la voie principale d'enregistrement (espèces, virement, chèque, carte, ou mobile money confirmé a posteriori) ; le flux en ligne (`POST /payments/checkout` + `POST /payments/webhooks/{provider}`, ce dernier public et protégé uniquement par vérification de signature) est additif, pas un remplacement — le frontend Angular existant n'a pas été modifié pour l'exposer.

## État du projet

Phase 1 (socle) + les **13 modules métier d'origine** complets : **Clients**, **Suppliers**, **Products**, **Services**, **Quotes** (devis), **Invoices** (factures), **Payments** (paiements), **Accounting** (comptabilité), **Reports** (rapports), **Users/Roles** (comptes utilisateurs), **Settings** (configuration entreprise), **Audit** (piste d'audit) et **Dashboard** (tableau de bord). Le backend couvre désormais l'intégralité du périmètre initial du frontend Angular.

Dashboard (`GET /dashboard/kpis`, `/revenue-chart`, `/revenue-chart/status`, `/recent-activity`) est accessible à tout utilisateur authentifié, sans permission dédiée (conforme au frontend : aucune entrée `Permission` ni garde de rôle sur cette page). KPIs et graphiques calculés à partir de données réelles (`InvoiceRepository`/`PaymentRepository`/`ClientRepository`) plutôt que les valeurs codées en dur du mock : `revenue`/`invoicesCount` du mois courant avec tendance vs mois précédent, `receivables` = solde des factures SENT/PARTIALLY_PAID moins les paiements complétés associés, `activeClients` = comptage réel. Le graphique de statut des factures reclasse à la volée les factures SENT/PARTIALLY_PAID en échéance dépassée vers un bucket OVERDUE (même logique que le statut calculé d'Invoices), plutôt qu'un simple group-by sur le statut persisté. `recent-activity` réutilise directement le module Audit (les 10 dernières entrées de `audit_logs`, traduites en message français lisible) plutôt que de dupliquer un mécanisme de suivi d'activité séparé. Deux champs du contrat frontend (`payables`, `productsSold`) n'ont aucune source de données réelle dans ce backend (aucun sous-système de comptes fournisseurs, aucun lien facture-produit) — fixés à zéro et documentés plutôt que fabriqués. Vérifié en direct : cohérence croisée confirmée entre `revenue-chart` (mois courant) et `kpis.revenue`, et entre la somme des buckets de `revenue-chart/status` et `kpis.invoicesCount`.

Audit est intégralement en lecture seule (`GET /audit-logs`, gate `audit:read`, ADMIN + AUDITEUR) et repose sur un mécanisme transversal générique : n'importe quel module publie un `AuditableActionEvent(action, entityType, entityLabel, details?, actorUserId)` via `ApplicationEventPublisher`, et `AuditLogRecordingService` (écouteur `@TransactionalEventListener(phase = AFTER_COMMIT)`, avec `@Transactional(propagation = REQUIRES_NEW)` appliqué dès le départ cette fois — leçon d'Accounting) l'enregistre dans une vraie table `audit_logs`, en résolvant le nom d'affichage de l'acteur via `UserRepository.findById(actorUserId)`. Câblé sur la quasi-totalité du backend : CREATE/UPDATE/DELETE sur Clients/Suppliers/Products/Services/Quotes/Invoices/Payments/Users, VALIDATE sur la validation de facture, LOGIN sur l'authentification, UPDATE sur Settings. Vérifié en direct pour chaque module instrumenté (connexion → entrée LOGIN immédiate ; création client/facture/produit/fournisseur → entrées CREATE ; mise à jour → UPDATE ; suppression → DELETE ; validation de facture → VALIDATE), avec RBAC confirmé (403 pour un rôle GESTIONNAIRE).

Le smoke test a révélé un bug réel dans le seul champ triable de la page Audit (`timestamp`, via `mat-sort-header`) : `GET /audit-logs?sort=timestamp` renvoyait 500 (`PropertyReferenceException: No property 'timestamp' found for type 'AuditLog'`), le DTO exposant `timestamp` alors que l'entité JPA stocke `occurredAt` — `PageableFactory` transmet le paramètre `sort` tel quel à Spring Data, qui le résout contre les propriétés de l'**entité**, pas du DTO. En vérifiant si ce même défaut existait ailleurs, `GET /users?sort=isActive` s'est avéré avoir exactement le même problème (`IUser.isActive` vs `User.active`). Corrigé dans les deux contrôleurs par une traduction explicite nom-DTO → nom-entité avant construction du `Pageable`. Règle retenue : tout champ de tri exposé publiquement dont le nom diffère du nom de propriété de l'entité JPA sous-jacente doit être traduit explicitement au niveau du contrôleur — jamais supposé identique sans vérification. Settings est une ligne singleton unique (`GET/PUT /settings/company`, `settings:manage`, ADMIN uniquement) : `companyName`/`address`/`taxId`/`currency`/`defaultTaxRate` sont purement informatifs, mais `invoicePrefix`/`quotePrefix` pilotent **réellement** `InvoiceService`/`QuoteService` via `CompanySettingsService.getInvoicePrefix()`/`getQuotePrefix()` — remplace les constantes codées en dur `"FAC"`/`"DEV"` des modules Invoices/Quotes (décision produit assumée : le frontend expose ces champs dans un formulaire mais ne les consomme nulle part au runtime ; les rendre actifs évite un formulaire Settings trompeur). Vérifié en direct : changer `invoicePrefix` en `"INV"` puis créer une facture produit immédiatement `INV-2026-00XX`. Users/Roles est reservé aux administrateurs (`user:manage`, seul `ADMIN` le possede) : CRUD complet (`GET/POST/PUT/DELETE /users`) plus `POST /users/{id}/reset-password` (reinitialisation admin, sans jeton, journalise en DEV_ONLY comme `LoggingPasswordResetNotifier`). Les rôles restent une énumération fixe côté frontend (`role-matrix`, lecture seule) — aucun endpoint `roles` n'existe côté backend, conforme au contrat. Ajouté au-delà du mock (qui ne les vérifie jamais) : doublon d'email (409, même pattern que Clients/Suppliers/Products) et protection du dernier administrateur actif (409 `INVALID_STATE` si une mise à jour ou suppression retirerait le rôle ADMIN actif au dernier compte qui le détient) — sans cette garde, un ADMIN unique aurait pu se verrouiller lui-même hors de toute gestion des comptes. Payments fait évoluer automatiquement le statut de la facture (`SENT` → `PARTIALLY_PAID` → `PAID`, et inversement lors d'un remboursement) via `PaymentRepository.sumCompletedAmountByInvoiceId`, remplaçant le `PUT` manuel utilisé jusqu'ici pour simuler un règlement. Accounting est intégralement en lecture seule côté API (`GET /accounting/journal`, `/journal/accounts`, `/ledger/{code}`, `/trial-balance`, `/cashbook`) ; les écritures sont générées automatiquement en partie double par `JournalEntryRecordingService`, qui écoute `InvoiceValidatedEvent`/`PaymentReceivedEvent`/`PaymentRefundedEvent` publiés par `InvoiceService`/`PaymentService` via `@TransactionalEventListener(phase = AFTER_COMMIT)`.

Reports est également intégralement en lecture seule (`GET /reports/sales-by-client`, `/aging-receivables`, `/quote-funnel`, gate `report:read`), sans données propres : trois agrégations JPQL calculées à la volée sur `Invoice`/`Quote` (`InvoiceRepository.aggregateSalesByClient`/`findOverdueInvoices`, `QuoteRepository.aggregateByStatus`), exposées via des projections dédiées (`invoices/dto/SalesByClientProjection`, `AgingReceivableProjection`, `quotes/dto/QuoteFunnelProjection`) puis mappées vers les DTOs finaux du module `reports`. Le smoke test avec des `startDate`/`endDate` réels (jamais exercé auparavant, même pour Accounting) a révélé un bug latent partagé : le motif JPQL `(:startDate is null or ...)` fait échouer PostgreSQL avec `could not determine data type of parameter $1` dès qu'un des deux bornes est fournie — le pilote ne peut pas déduire le type d'un paramètre dont la seule occurrence directe est un test `IS NULL`. Corrigé par un cast explicite `cast(:startDate as date)` dans **les deux** requêtes concernées (`InvoiceRepository.aggregateSalesByClient`, nouvellement écrite, et `JournalEntryRepository.aggregateByAccount` d'Accounting, déjà en production mais jamais testée avec un intervalle réel). Les endpoints basés sur `Specification` (`accounting/journal`, `/ledger`, `/cashbook`) n'ont jamais eu ce problème : une `Specification` omet purement le prédicat quand la valeur est nulle, plutôt que d'envoyer un paramètre ambigu à Postgres.

Les huit modules ont été vérifiés bout-en-bout via `docker compose up` + `curl` réel, y compris la chaîne complète paiement partiel → paiement complet → remboursement → statut facture recalculé à chaque étape, et pour Accounting le scénario facture créée → validée → payée → remboursée avec vérification que les écritures comptables apparaissent réellement (pas seulement testable unitairement, puisque `@TransactionalEventListener` exige un vrai contexte transactionnel Spring). Ce test bout-en-bout a révélé un bug réel : les listeners `AFTER_COMMIT` appelaient `journalEntryRepository.saveAll(...)` sans `@Transactional(propagation = REQUIRES_NEW)` — au moment où Spring invoque un listener AFTER_COMMIT, les ressources de la transaction d'origine (déjà physiquement validée) sont encore liées au thread, donc l'appel au repository "participe" à cette transaction fantôme au lieu d'en ouvrir une nouvelle : les écritures étaient silencieusement perdues (`persist()` en mémoire, jamais flush ni commit, aucune exception levée, aucune ligne dans les logs). Corrigé en ajoutant `@Transactional(propagation = Propagation.REQUIRES_NEW)` sur les trois méthodes d'écoute de `JournalEntryRecordingService`. Prochain module à répliquer selon le même pattern : Reports.
