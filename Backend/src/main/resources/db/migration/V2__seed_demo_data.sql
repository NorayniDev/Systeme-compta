-- Donnees de demonstration pour l'environnement de developpement.
-- Mot de passe en clair pour le compte admin : Admin123!
INSERT INTO users (first_name, last_name, email, password_hash, role, is_active, created_by, updated_by)
VALUES ('Admin', 'Demo', 'admin@facturation-pme.sn',
        '$2y$12$/05ZkNxCVgZHLzvryOmz5ev4hdDpcZ5d/3Z2NZwK8pB4z7x4nP7.q',
        'ADMIN', TRUE, 'system', 'system');

INSERT INTO clients (name, email, phone, address, tax_id, status, total_invoiced, created_by, updated_by)
VALUES
    ('ACME Senegal SARL', 'contact@acme.sn', '+221 77 111 22 33', 'Rue 10, Plateau, Dakar, Senegal', 'NINEA0012233', 'ACTIVE', 4012000, 'system', 'system'),
    ('Casamance Fruits SA', 'contact@casamance-fruits.sn', '+221 77 222 33 44', 'Ziguinchor, Senegal', 'NINEA0033445', 'ACTIVE', 1200000, 'system', 'system'),
    ('Baobab Distribution', 'contact@baobab-distribution.sn', '+221 77 333 44 55', 'Thies, Senegal', 'NINEA0044556', 'ACTIVE', 1121000, 'system', 'system');
