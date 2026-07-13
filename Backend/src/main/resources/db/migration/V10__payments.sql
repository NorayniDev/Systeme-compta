CREATE TABLE payments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference           VARCHAR(50)    NOT NULL,
    invoice_id          UUID           NOT NULL REFERENCES invoices (id) ON DELETE RESTRICT,
    invoice_number      VARCHAR(50)    NOT NULL,
    client_id           UUID           NOT NULL REFERENCES clients (id) ON DELETE RESTRICT,
    client_name         VARCHAR(255)   NOT NULL,
    amount              NUMERIC(14, 2) NOT NULL,
    method              VARCHAR(20)    NOT NULL,
    status              VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    paid_at             TIMESTAMPTZ    NOT NULL,
    notes               VARCHAR(2000),
    gateway_provider    VARCHAR(30),
    gateway_session_id  VARCHAR(100),
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    CONSTRAINT uk_payments_reference UNIQUE (reference),
    CONSTRAINT uk_payments_gateway_session_id UNIQUE (gateway_session_id)
);

CREATE INDEX idx_payments_invoice_id ON payments (invoice_id);
CREATE INDEX idx_payments_status ON payments (status);

-- Compteur de numerotation deja utilise par le paiement de demonstration ci-dessous.
INSERT INTO document_number_counters (doc_type, year, last_value) VALUES ('PAYMENT', 2026, 1);

INSERT INTO payments (reference, invoice_id, invoice_number, client_id, client_name, amount, method, status, paid_at, notes, created_by, updated_by)
SELECT 'PAY-2026-0001', i.id, i.number, i.client_id, i.client_name, i.total_amount, 'BANK_TRANSFER', 'COMPLETED', TIMESTAMPTZ '2026-05-28 10:00:00+00', 'Reglement par virement bancaire.', 'system', 'system'
FROM invoices i WHERE i.number = 'FAC-2026-0001';
