-- Un utilisateur de demonstration par role (en plus de l'admin seede en V2), pour tester le RBAC
-- manuellement sans passer par le module Users. Mots de passe en clair : voir CREDENTIALS.md.
INSERT INTO users (first_name, last_name, email, password_hash, role, is_active, created_by, updated_by)
VALUES
    ('Fatou', 'Sow', 'comptable@facturation-pme.sn',
     '$2a$12$0/5DvlUcg4T2f0yVPROYneCPhXqP29WQt6L2fNF5QlOjSHvRW417e',
     'COMPTABLE', TRUE, 'system', 'system'),
    ('Moussa', 'Ndiaye', 'gestionnaire@facturation-pme.sn',
     '$2a$12$v3NsHgRH4ce/JJqZz8ZSyeAp.om1ILMcRkSfOK723AXkSw763aPG2',
     'GESTIONNAIRE', TRUE, 'system', 'system'),
    ('Aissatou', 'Diallo', 'caissier@facturation-pme.sn',
     '$2a$12$s0.0AHCnV3riqWhrXIWcaeNm4trEkf0ZFjZMroUMctSN2qvNghOlG',
     'CAISSIER', TRUE, 'system', 'system'),
    ('Ibrahima', 'Fall', 'auditeur@facturation-pme.sn',
     '$2a$12$rnrKa1W1WNLNiXI/LzITveys/xb0jeFpZ1cU6OMZeKP6.ddooC/tC',
     'AUDITEUR', TRUE, 'system', 'system'),
    ('Cheikh', 'Ba', 'utilisateur@facturation-pme.sn',
     '$2a$12$2dxsGz9NtJ17W9NNBe0rL.swxf18qd6OduIN99zCPHluuHR88nU4m',
     'UTILISATEUR', TRUE, 'system', 'system');
