# Comptes de démonstration

Identifiants des comptes seedés par les migrations Flyway du backend (`Backend/src/main/resources/db/migration/V2__seed_demo_data.sql` et `V14__seed_demo_users_per_role.sql`). Valables uniquement sur l'environnement de développement local (`docker compose up`).

URL de connexion : `POST http://localhost:8080/api/v1/auth/login`

| Rôle | Email | Mot de passe | Nom |
|---|---|---|---|
| ADMIN | `admin@facturation-pme.sn` | `Admin123!` | Admin Demo |
| COMPTABLE | `comptable@facturation-pme.sn` | `Comptable123!` | Fatou Sow |
| GESTIONNAIRE | `gestionnaire@facturation-pme.sn` | `Gestionnaire123!` | Moussa Ndiaye |
| CAISSIER | `caissier@facturation-pme.sn` | `Caissier123!` | Aissatou Diallo |
| AUDITEUR | `auditeur@facturation-pme.sn` | `Auditeur123!` | Ibrahima Fall |
| UTILISATEUR | `utilisateur@facturation-pme.sn` | `Utilisateur123!` | Cheikh Ba |

## Exemple

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"comptable@facturation-pme.sn","password":"Comptable123!"}'
```

## ⚠️ Ne jamais utiliser en production

Ces comptes et mots de passe sont uniquement destinés au développement et à la démonstration locale. Ne pas réutiliser ces identifiants, ni ce fichier, dans un environnement de production.
