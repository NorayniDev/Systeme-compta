CREATE TABLE audit_logs (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    occurred_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    user_name     VARCHAR(255) NOT NULL,
    action        VARCHAR(20)  NOT NULL,
    entity_type   VARCHAR(100) NOT NULL,
    entity_label  VARCHAR(255) NOT NULL,
    details       VARCHAR(500)
);

CREATE INDEX idx_audit_logs_occurred_at ON audit_logs (occurred_at DESC);

INSERT INTO audit_logs (occurred_at, user_name, action, entity_type, entity_label, details)
VALUES
    (now() - interval '5 days', 'Admin Demo', 'LOGIN', 'Auth', 'Connexion', NULL),
    (now() - interval '5 days', 'Admin Demo', 'CREATE', 'Facture', 'FAC-2026-0001', NULL),
    (now() - interval '4 days', 'Admin Demo', 'VALIDATE', 'Facture', 'FAC-2026-0001', NULL),
    (now() - interval '3 days', 'Admin Demo', 'UPDATE', 'Client', 'ACME Senegal SARL', 'Changement de statut : ACTIF'),
    (now() - interval '2 days', 'Admin Demo', 'CREATE', 'Paiement', 'PAY-2026-0001', NULL),
    (now() - interval '1 days', 'Admin Demo', 'DELETE', 'Devis', 'DEV-2026-0003', NULL);
