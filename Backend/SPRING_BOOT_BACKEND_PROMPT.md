# Prompt d'initialisation — Backend Spring Boot (Système Intelligent de Facturation et de Comptabilité pour les PME)

> À copier-coller tel quel dans une nouvelle session (nouveau dossier `backend/`) pour démarrer le développement du backend. Ce prompt fixe le contrat d'API et les standards de code attendus. Le frontend Angular 20 existe déjà et **ne doit pas être modifié** pour s'adapter au backend — c'est l'inverse qui est vrai : chaque endpoint, DTO, enum et code d'erreur ci-dessous a été extrait du frontend réel (`core/constants/api-endpoints.constant.ts`, `core/constants/roles.constant.ts`, `core/models/*`, `features/*/models/*`) et doit être respecté à l'identique.

---

## 0. Contexte et objectif

Construis le backend Spring Boot du **Système Intelligent de Facturation et de Comptabilité pour les PME**, une plateforme SaaS déjà dotée d'un frontend Angular 20 complet (Standalone Components + Signals, 13 modules métier, RBAC à 6 rôles, mode démo avec API mockée). Ce backend doit remplacer le mock (`environment.useMockApi = false`) sans qu'aucun code Angular n'ait à changer : mêmes routes, mêmes noms de champs JSON, mêmes formats de pagination et d'erreurs.

Le code doit être **de qualité entreprise** : celui qu'on attend en revue de code dans une grande société (banque, éditeur SaaS), pas un prototype. Priorité à la correction, la sécurité, la testabilité et la lisibilité sur la vitesse d'écriture.

Travaille **module par module**, comme pour le frontend : socle d'abord (Phase 1), puis un module métier à la fois, avec vérification (compilation, tests, appel réel via curl/Postman) avant de passer au suivant. Ne construis pas les 13 modules d'un coup.

---

## 1. Stack technique imposée

- **Java 21** (LTS), **Spring Boot 3.3+**
- Build : **Maven** (wrapper `mvnw` committé)
- **Spring Web** (MVC, pas WebFlux — le frontend fait du request/response classique, pas de streaming)
- **Spring Data JPA** + **Hibernate**
- **PostgreSQL** en dev/prod, **Flyway** pour les migrations (pas de `ddl-auto: update` en dehors de tests)
- **Spring Security 6** + **JJWT** (ou `spring-security-oauth2-resource-server` en mode JWT auto-émis) pour l'auth JWT access+refresh
- **Bean Validation** (`jakarta.validation`) pour les DTO d'entrée
- **MapStruct** pour le mapping Entity ↔ DTO (pas de mapping manuel répétitif)
- **springdoc-openapi** (Swagger UI) pour la documentation API générée depuis le code
- **Lombok** (autorisé, avec parcimonie : `@Getter/@Setter/@Builder` sur les entités/DTO, jamais `@Data` sur les entités JPA à cause d'`equals/hashCode`)
- Tests : **JUnit 5**, **Mockito**, **Testcontainers** (PostgreSQL réel en test d'intégration), **AssertJ**
- Qualité : **Checkstyle** (ruleset Google ou Sun adapté), **Spotless** (formatage automatique, gate de build), **JaCoCo** (couverture, seuil minimum 80% sur `service`/`domain`)
- **Docker** (image multi-stage) + **docker-compose** (backend + PostgreSQL) pour un environnement de dev reproductible
- Logs : **SLF4J + Logback**, format JSON en profil `prod`

---

## 2. Architecture & structure de dossiers

Architecture **en couches par module métier** (package-by-feature, symétrique du frontend `features/*`), pas package-by-layer global. Chaque module est un package autonome avec ses propres sous-packages techniques.

```
com.facturationpme
├── FacturationPmeApplication.java
├── common/                        Transverse (équivalent de core/ côté Angular)
│   ├── config/                     SecurityConfig, CorsConfig, OpenApiConfig, JacksonConfig
│   ├── security/                    JwtService, JwtAuthenticationFilter, UserDetailsServiceImpl
│   ├── exception/                   GlobalExceptionHandler, ApiException, ResourceNotFoundException,
│   │                                 ValidationException, ForbiddenActionException
│   ├── dto/                          PageResponse<T>, ApiErrorResponse
│   ├── audit/                        AuditLogService, @Auditable (annotation + aspect AOP), AuditingConfig
│   └── util/                         Constantes partagées, mapper de pagination
├── auth/                            login, refresh, forgot/reset password
│   ├── AuthController, AuthService, dto/ (LoginRequest, LoginResponse, RefreshTokenRequest, ...)
├── users/                           comptes utilisateurs (IUser côté front)
│   ├── domain/User.java (entité), UserRole (enum), Permission (enum)
│   ├── repository/UserRepository
│   ├── service/UserService
│   ├── web/UserController
│   └── dto/UserCreateDto, UserUpdateDto, UserResponse
├── clients/  suppliers/  products/  services/     CRUD symétriques
├── quotes/                          Quote + QuoteLine (agrégat), cycle de vie, conversion en facture
├── invoices/                        Invoice + InvoiceLine (agrégat), validation, envoi
├── payments/                        Payment lié à une Invoice, calcul auto du solde facture
├── accounting/                       Account, JournalEntry — LECTURE SEULE côté API, généré par des
│                                     domain events (InvoiceValidated, PaymentReceived, ...)
├── reports/                          Requêtes agrégées en lecture seule (sales-by-client, aging, funnel)
├── audit/                            Exposition en lecture seule de l'AuditLog (alimenté par common/audit)
└── settings/                         CompanySettings (singleton), profil utilisateur courant
```

Dans chaque module :
```
<module>/
  domain/        Entités JPA (+ enums du module)
  repository/     Interfaces Spring Data JPA
  service/        Logique métier (interface + impl si la logique est non triviale, sinon classe unique)
  web/            @RestController (fin, délègue tout au service)
  dto/            Records Java pour requêtes/réponses
  mapper/         Interfaces MapStruct
```

Règles :
- Un contrôleur ne contient **aucune logique métier** : validation de forme (via `@Valid`) + délégation au service + mapping DTO via MapStruct.
- Un service ne connaît **jamais** les objets HTTP (`HttpServletRequest`, etc.) ni les DTO web — il prend/retourne soit des entités, soit des objets de commande dédiés (`record CreateClientCommand(...)`), au choix, mais reste découplé de la couche web.
- Pas de logique métier dans les entités JPA au-delà des invariants triviaux (ex : recalcul d'un total dérivé) — pas de "anemic vs rich model" dogmatique, mais rien qui dépende d'un repository ou d'un service.
- Dépendances toujours vers l'intérieur : `web → service → repository/domain`, jamais l'inverse. Un test d'architecture **ArchUnit** doit vérifier cette règle (`common/config` excepté).

---

## 3. Conventions de code

- Classes : `PascalCase`. Méthodes/variables : `camelCase`. Constantes : `UPPER_SNAKE_CASE`.
- **Pas de préfixe `I` sur les interfaces** (convention Angular du frontend, pas convention Java) : `ClientService`, pas `IClientService`. Si une interface a besoin d'être distinguée de son implémentation, suffixer l'impl : `ClientServiceImpl`.
- DTO nommés `XxxCreateDto`, `XxxUpdateDto`, `XxxResponse` (ou `XxxDto` pour un objet unique bidirectionnel simple) — **records Java immuables**, jamais de classes mutables pour les DTO.
- `UpdateDto` reprend exactement la forme de `CreateDto` + les champs supplémentaires modifiables (ex : `status`), **comme dans le frontend** (`ClientUpdateDto extends ClientCreateDto` côté TS → côté Java, dupliquer les champs dans le record, MapStruct fait le lien).
- Pas de commentaires Javadoc qui répètent la signature. Un commentaire uniquement quand une règle métier ou une contrainte non évidente le justifie.
- `Optional<T>` en retour de repository/service quand l'absence est un cas normal, jamais en paramètre.
- Pas de `null` en retour de méthode publique — `Optional`, collection vide, ou exception métier.
- Pas de logique dupliquée entre modules CRUD symétriques (`clients`/`suppliers` notamment) : factoriser dans `common` si le besoin est réel (ex : un `AbstractCrudService<T, ID>` générique), sans sur-architecturer avant d'avoir au moins 2 cas concrets.

---

## 4. Sécurité & RBAC — doit correspondre EXACTEMENT au frontend

### 4.1 Rôles et permissions

```java
public enum UserRole { ADMIN, COMPTABLE, GESTIONNAIRE, CAISSIER, AUDITEUR, UTILISATEUR }

public enum Permission {
    INVOICE_CREATE("invoice:create"), INVOICE_READ("invoice:read"),
    INVOICE_UPDATE("invoice:update"), INVOICE_DELETE("invoice:delete"),
    INVOICE_VALIDATE("invoice:validate"),
    QUOTE_CREATE("quote:create"), QUOTE_READ("quote:read"),
    QUOTE_UPDATE("quote:update"), QUOTE_DELETE("quote:delete"),
    PAYMENT_CREATE("payment:create"), PAYMENT_READ("payment:read"), PAYMENT_REFUND("payment:refund"),
    CLIENT_MANAGE("client:manage"), SUPPLIER_MANAGE("supplier:manage"),
    PRODUCT_MANAGE("product:manage"), SERVICE_MANAGE("service:manage"),
    ACCOUNTING_READ("accounting:read"), ACCOUNTING_MANAGE("accounting:manage"),
    REPORT_READ("report:read"), REPORT_EXPORT("report:export"),
    USER_MANAGE("user:manage"), ROLE_MANAGE("role:manage"),
    SETTINGS_MANAGE("settings:manage"), AUDIT_READ("audit:read");

    private final String value;
    Permission(String value) { this.value = value; }
    @JsonValue public String getValue() { return value; }
}
```

⚠️ **Point critique** : `Permission` DOIT sérialiser en JSON sous la forme `"invoice:create"`, pas `"INVOICE_CREATE"` — d'où le `@JsonValue` obligatoire. Le frontend compare ces chaînes littéralement (`ROLE_PERMISSIONS`, directive `*appHasPermission`).

La matrice rôle→permissions par défaut (`ROLE_PERMISSIONS` côté frontend, fichier `core/constants/roles.constant.ts`) doit être répliquée côté backend comme source de vérité serveur (le frontend ne l'utilise qu'en repli si le backend ne renvoie pas les permissions). Recopier fidèlement la matrice existante — ne pas l'inventer.

Sécuriser chaque endpoint avec `@PreAuthorize("hasAuthority('invoice:create')")` (méthode) plutôt qu'uniquement en config globale, pour rester lisible module par module. Activer `@EnableMethodSecurity`.

### 4.2 JWT

- Access token courte durée (ex : 15 min), refresh token longue durée (ex : 7 jours), stocké en base (table `refresh_tokens`) pour permettre la révocation (logout, changement de mot de passe).
- Claims du JWT (doivent correspondre à `IJwtPayload` côté frontend) :
  ```json
  { "sub": "<userId>", "email": "...", "role": "ADMIN", "permissions": ["invoice:create", "..."], "iat": ..., "exp": ... }
  ```
- Mots de passe : `BCryptPasswordEncoder` (force ≥ 12).
- CORS : autoriser explicitement l'origine du frontend (`http://localhost:4200` en dev), pas de wildcard `*` en prod.
- Filtrer les endpoints `auth/login`, `auth/refresh`, `auth/forgot-password`, `auth/reset-password` en `permitAll()`, tout le reste authentifié par défaut (deny-by-default).

---

## 5. Contrat d'API générique — non négociable

### 5.1 Pagination

Le frontend attend cette forme exacte pour toute liste paginée (`core/models/pagination.model.ts::IPage<T>`) :

```json
{
  "content": [ ... ],
  "totalElements": 42,
  "totalPages": 5,
  "number": 0,
  "size": 10,
  "first": true,
  "last": false
}
```

Ne **jamais** renvoyer l'objet `Page<T>` natif de Spring Data tel quel (il contient `pageable`, `sort`, `empty`, `numberOfElements` en plus, ce qui est acceptable niveau parsing JSON côté TS mais pollue le contrat) : mapper systématiquement vers un `record PageResponse<T>(List<T> content, long totalElements, int totalPages, int number, int size, boolean first, boolean last)` dans `common/dto`, avec une factory statique `PageResponse.of(Page<T> page)`.

Paramètres de requête pour les listes (`ISearchFilter` côté front) : `page` (0-based), `size`, `sort`, `direction` (`asc`/`desc`), `query` (recherche libre). Le contrôleur les convertit en `Pageable` Spring Data + `Specification`/`Predicate` pour la recherche.

### 5.2 Réponses de succès — PAS d'enveloppe

Les réponses de succès renvoient **directement** la ressource ou le `PageResponse`, **sans wrapper `{ data, message, timestamp }`** — c'est ainsi que le mock frontend actuel répond et que `ApiService` (frontend) les consomme (`this.http.get<T>(url)`). Ne pas envelopper.

### 5.3 Réponses d'erreur — format imposé

Le frontend (`core/interceptors/error.interceptor.ts`) attend ce format JSON en cas d'erreur (`IApiError`) :

```json
{
  "status": 422,
  "code": "VALIDATION_ERROR",
  "message": "Des erreurs de validation sont survenues.",
  "errors": { "email": ["doit être une adresse valide"], "name": ["ne doit pas être vide"] },
  "timestamp": "2026-07-08T10:15:30Z",
  "path": "/api/v1/clients"
}
```

Implémenter via un unique `@RestControllerAdvice` (`common/exception/GlobalExceptionHandler`) couvrant : `MethodArgumentNotValidException` (422, `errors` peuplé champ par champ), `ResourceNotFoundException` (404), `ForbiddenActionException`/`AccessDeniedException` (403), `AuthenticationException` (401), toute autre exception non gérée (500, message générique — ne jamais fuiter la stacktrace au client, la logger côté serveur).

Le comportement frontend actuel sur ces codes : 401 → tentative de refresh silencieux puis retry ; 403 → redirection `/errors/403` ; 404 → `/errors/404` ; 500+ → `/errors/500`. Le backend doit donc renvoyer des codes HTTP corrects et cohérents pour déclencher la bonne UX.

### 5.4 Base path

Toutes les routes sont préfixées `/api/v1/` (aligné sur `environment.apiUrl` du frontend). Exemple : `GET /api/v1/clients`.

### 5.5 Endpoints exacts à exposer (repris de `api-endpoints.constant.ts`)

```
POST   auth/login
POST   auth/logout
POST   auth/refresh
GET    auth/me
POST   auth/forgot-password
POST   auth/reset-password

GET    users                     POST users
GET    users/{id}   PUT users/{id}   DELETE users/{id}
POST   users/{id}/reset-password

GET    roles                     (lecture seule — matrice fixe)

GET    clients                   POST clients
GET    clients/{id}  PUT clients/{id}  DELETE clients/{id}

GET    suppliers  ...             (symétrique de clients)
GET    products   ...             (symétrique)
GET    services   ...             (symétrique)

GET    quotes                    POST quotes
GET    quotes/{id}  PUT quotes/{id}  DELETE quotes/{id}
POST   quotes/{id}/convert-to-invoice   (action métier)

GET    invoices                  POST invoices
GET    invoices/{id}  PUT invoices/{id}  DELETE invoices/{id}
POST   invoices/{id}/validate
POST   invoices/{id}/send

GET    payments                  POST payments
GET    payments/{id}
POST   payments/{id}/refund

GET    accounting/journal
GET    accounting/ledger
GET    accounting/trial-balance
GET    accounting/cashbook

GET    dashboard/kpis
GET    dashboard/revenue-chart
GET    dashboard/recent-activity

GET    reports/sales-by-client
GET    reports/aging-receivables
GET    reports/quote-funnel

GET    audit-logs

GET    notifications

GET    profile               PUT profile
POST   profile/change-password

GET    settings/company      PUT settings/company
```

---

## 6. Domaine métier — champs exacts (extraits des modèles TypeScript réels)

Pour chaque module ci-dessous : `id` = `UUID` (généré serveur), tous les timestamps en `Instant`/`OffsetDateTime` sérialisés ISO-8601 (`string` côté front), tous les montants/quantités en `BigDecimal` côté Java (persistance et calculs) mais exposés comme `number` JSON standard (comportement par défaut de Jackson, ne rien changer), tous les enums sérialisés par leur nom Java (`.name()`), sauf `Permission` (voir §4.1).

### Client
```
IClient { id, name, email, phone, address, taxId, status: ClientStatus[ACTIVE|INACTIVE], totalInvoiced, createdAt, updatedAt }
ClientCreateDto { name, email, phone, address, taxId }
ClientUpdateDto extends ClientCreateDto { status }
```
`totalInvoiced` est une valeur dérivée (somme des factures non annulées du client) — recalculée par le service, jamais transmise en écriture.

### Supplier (symétrique de Client)
```
ISupplier { id, name, email, phone, address, taxId, status: SupplierStatus[ACTIVE|INACTIVE], totalPurchased, createdAt, updatedAt }
```

### Product
```
IProduct { id, name, sku, description, category, unitPrice, taxRate, unit: ProductUnit[PIECE|HOUR|KG|LITER|METER|BOX], stockQuantity, status: ProductStatus[ACTIVE|INACTIVE], createdAt, updatedAt }
```

### ServiceItem
```
IServiceItem { id, name, code, description, category, unitPrice, taxRate, unit: ServiceItemUnit[HOUR|DAY|MONTH|FLAT_RATE], status: ServiceItemStatus[ACTIVE|INACTIVE], createdAt, updatedAt }
```

### Quote (agrégat avec lignes)
```
QuoteStatus = DRAFT|SENT|ACCEPTED|REJECTED|EXPIRED|CONVERTED
IQuoteLine { id?, description, quantity, unitPrice, taxRate, lineTotal }
IQuote { id, number, clientId, clientName, issueDate, validUntil, lines: IQuoteLine[], amountExclTax, taxAmount, totalAmount, status, notes?, convertedInvoiceId?, createdAt, updatedAt }
```
`number` = référence métier générée serveur (préfixe configurable via `CompanySettings.quotePrefix`, ex : `DEV-2026-0001`). `lineTotal`, `amountExclTax`, `taxAmount`, `totalAmount` sont **recalculés serveur** à chaque écriture, jamais fait confiance à ce qu'envoie le client (le frontend les calcule aussi côté UI pour l'affichage instantané, mais le backend est la seule source de vérité).

### Invoice (agrégat avec lignes)
```
InvoiceStatus = DRAFT|SENT|PAID|PARTIALLY_PAID|OVERDUE|CANCELLED
IInvoiceLine { id?, description, quantity, unitPrice, taxRate, lineTotal }
IInvoice { id, number, clientId, clientName, issueDate, dueDate, lines: IInvoiceLine[], amountExclTax, taxAmount, totalAmount, status, notes?, createdAt, updatedAt }
```
Règles métier :
- `POST invoices/{id}/validate` : `DRAFT → SENT`, génère les écritures comptables (débit compte client / crédit compte de vente + TVA collectée), déclenche un `AuditLog` (`VALIDATE`).
- Un paiement complet passe la facture à `PAID`, un paiement partiel à `PARTIALLY_PAID` (recalcul du statut fait par `PaymentService`, jamais par le client de l'API).
- Un batch planifié (ou calcul à la volée en lecture) marque `OVERDUE` toute facture `SENT`/`PARTIALLY_PAID` dont `dueDate < now`.

### Payment
```
PaymentMethod = CASH|BANK_TRANSFER|MOBILE_MONEY|CHECK|CARD
PaymentStatus = PENDING|COMPLETED|FAILED|REFUNDED
IPayment { id, reference, invoiceId, invoiceNumber, clientId, clientName, amount, method, status, paidAt, notes?, createdAt, updatedAt }
```
`POST payments/{id}/refund` : passe le paiement à `REFUNDED`, régénère l'écriture comptable inverse, ne supprime jamais l'historique.

### Accounting (LECTURE SEULE côté API — aucun endpoint POST/PUT/DELETE)
```
AccountType = ASSET|LIABILITY|EQUITY|REVENUE|EXPENSE
JournalSource = INVOICE|PAYMENT|MANUAL
IAccount { code, name, type }
IJournalEntry { id, date, reference, accountCode, accountName, label, debit, credit, source }
ITrialBalanceLine { accountCode, accountName, totalDebit, totalCredit, balance }
```
Les écritures sont produites exclusivement par des **domain events** internes (`InvoiceValidatedEvent`, `PaymentReceivedEvent`, `PaymentRefundedEvent`) écoutés par un `JournalEntryService` — jamais par un contrôleur exposé. Invariant vérifiable en test : somme des débits = somme des crédits sur toute période (`accounting/trial-balance`).

### User / Role
```
IUser { id, firstName, lastName, email, role: UserRole, permissions: Permission[], avatarUrl?, isActive, lastLoginAt?, createdAt }
```
`GET roles` renvoie la matrice `UserRole → Permission[]` en lecture seule (pas de CRUD — cohérent avec le frontend qui documente cette page comme non éditable par design, les rôles étant un enum fixe garanti par le typage des deux côtés).

### CompanySettings (singleton, une seule ligne en base)
```
ICompanySettings { companyName, address, taxId, currency, defaultTaxRate, invoicePrefix, quotePrefix }
```

### Reports (LECTURE SEULE, requêtes agrégées — pas d'entité dédiée, projections JPA/SQL natif)
```
ISalesByClientLine { clientId, clientName, invoiceCount, amountExclTax, taxAmount, totalAmount }
IAgingReceivableLine { invoiceId, invoiceNumber, clientName, dueDate, daysOverdue, amountDue }
IQuoteFunnelLine { status, count, totalAmount }
```

### Audit (LECTURE SEULE, alimenté automatiquement)
```
AuditAction = CREATE|UPDATE|DELETE|LOGIN|VALIDATE|EXPORT
IAuditLog { id, timestamp, userName, action, entityType, entityLabel, details? }
```
Alimenter cette table via une **annotation `@Auditable`** + aspect AOP (ou des domain events) posée sur les méthodes de service sensibles (création/modification/suppression/validation/login/export), pas par du code dupliqué dans chaque service. `AUDIT_READ` est la seule permission requise pour `GET audit-logs` (rôles `ADMIN`, `AUDITEUR`).

---

## 7. Authentification — contrat exact

```
POST auth/login
  Request  : { email, password, rememberMe }
  Response : { accessToken, refreshToken, expiresIn, user: IUser }

POST auth/refresh
  Request  : { refreshToken }
  Response : { accessToken, refreshToken, expiresIn, user: IUser }   (même forme que login)

POST auth/forgot-password
  Request  : { email }
  Response : 204 No Content (toujours, même si l'email n'existe pas — ne pas révéler l'existence d'un compte)

POST auth/reset-password
  Request  : { token, newPassword }
  Response : 204 No Content
```

---

## 8. Qualité, tests, outillage

- **Checkstyle** + **Spotless** exécutés en phase `verify` du build Maven, échec du build si non conforme.
- **JaCoCo** : rapport de couverture obligatoire, seuil minimum 80% sur `service` et `domain`, exclu sur `dto`/`config`.
- Tests unitaires (`*Test`, Mockito, pas de contexte Spring) pour tout `Service`.
- Tests d'intégration (`*IT`, `@SpringBootTest` + **Testcontainers PostgreSQL**) pour au moins un scénario bout-en-bout par module (ex : créer une facture → la valider → vérifier l'écriture comptable générée → vérifier l'entrée d'audit).
- **ArchUnit** : test qui échoue si un contrôleur accède directement à un repository, ou si une entité dépend d'un service.
- Documentation API : **springdoc-openapi**, Swagger UI exposé en dev (`/swagger-ui.html`), désactivé en prod ou protégé.
- Un `README.md` par module si la logique métier n'est pas triviale (ex : calcul des écritures comptables), sinon le code auto-documenté suffit.

---

## 9. Persistance & migrations

- PostgreSQL, schéma géré à 100% par **Flyway** (`src/main/resources/db/migration/V{n}__description.sql`), jamais `hibernate.ddl-auto=update` hors tests (`create-drop` acceptable uniquement en profil `test` avec H2 ou Testcontainers).
- Contraintes d'intégrité en base (pas seulement en Java) : `NOT NULL`, `UNIQUE` (ex : email client, SKU produit, référence facture), clés étrangères avec `ON DELETE RESTRICT` par défaut (pas de suppression en cascade silencieuse sur des données financières).
- Auditing technique standard via Spring Data JPA (`@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`) sur toutes les entités, en plus de l'`AuditLog` métier (les deux ont des rôles différents : l'un est technique/interne, l'autre est le journal métier exposé au frontend).

---

## 10. Observabilité

- **Spring Boot Actuator** : `/actuator/health`, `/actuator/info`, `/actuator/metrics` exposés (health détaillé réservé à un rôle admin ou réseau interne).
- Logs structurés JSON en profil `prod` (encoder Logback dédié), format lisible en `dev`.
- Un identifiant de corrélation (`X-Request-Id` ou équivalent) propagé dans le MDC pour tracer une requête de bout en bout dans les logs.

---

## 11. Docker & environnements

- `Dockerfile` multi-stage (build Maven dans une image, runtime JRE léger type `eclipse-temurin:21-jre-alpine` dans la seconde).
- `docker-compose.yml` : service `backend` + service `postgres` (volumes persistants, variables d'environnement pour les secrets — jamais de mot de passe en dur dans le compose committé).
- Profils Spring : `dev` (Postgres local, logs verbeux, Swagger actif), `test` (Testcontainers/H2), `prod` (logs JSON, Swagger désactivé, CORS strict).

---

## 12. Méthode de travail attendue

Comme pour le frontend, procède **incrémentalement** :

1. **Phase 1 — Socle** : structure Maven, config Spring Security + JWT (sans module métier encore), `GlobalExceptionHandler`, `PageResponse`, Flyway init, docker-compose, Actuator, Swagger. Vérifie que `POST auth/login` fonctionne avec un utilisateur seedé (migration Flyway de données de démo), que le token est valide et que `GET auth/me` renvoie l'utilisateur.
2. **Module Clients** en entier (domain, repository, service, controller, DTO, mapper, migrations, tests unitaires + IT) comme module de référence — pattern à répliquer ensuite.
3. Poursuis module par module dans cet ordre logique (chaque module s'appuie sur les précédents) : Suppliers → Products → Services → Quotes → Invoices → Payments → Accounting (généré par events) → Reports → Users/Roles → Settings → Audit → Dashboard.
4. Après chaque module : compilation (`mvn verify`), tests (unitaires + IT), puis un appel réel (`curl`/Postman/fichier `.http`) contre une instance lancée localement pour confirmer le contrat JSON exact face au frontend (comparer avec ce que renvoyait le mock Angular pour ce module).
5. Ne passe au module suivant qu'après validation explicite.

---

## 13. Livrable attendu pour cette première itération

Uniquement la **Phase 1 (socle)** + le **module Clients** complet. Pas plus. Fournis à la fin : arborescence du projet, commande pour lancer `docker-compose up`, commande `curl` de login + `curl` de `GET /api/v1/clients` authentifié, et un résumé des choix techniques pris qui ne sont pas explicitement dictés ci-dessus.
