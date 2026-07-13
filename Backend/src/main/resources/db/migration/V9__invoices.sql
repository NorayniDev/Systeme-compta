CREATE TABLE invoices (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    number           VARCHAR(50)    NOT NULL,
    client_id        UUID           NOT NULL REFERENCES clients (id) ON DELETE RESTRICT,
    client_name      VARCHAR(255)   NOT NULL,
    issue_date       DATE           NOT NULL,
    due_date         DATE           NOT NULL,
    amount_excl_tax  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    tax_amount       NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_amount     NUMERIC(14, 2) NOT NULL DEFAULT 0,
    status           VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    notes            VARCHAR(2000),
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by       VARCHAR(255),
    updated_by       VARCHAR(255),
    CONSTRAINT uk_invoices_number UNIQUE (number)
);

CREATE INDEX idx_invoices_client_id ON invoices (client_id);
CREATE INDEX idx_invoices_status ON invoices (status);

CREATE TABLE invoice_lines (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id  UUID          NOT NULL REFERENCES invoices (id) ON DELETE CASCADE,
    description VARCHAR(500)  NOT NULL,
    quantity    NUMERIC(12,2) NOT NULL,
    unit_price  NUMERIC(14,2) NOT NULL,
    tax_rate    NUMERIC(5,2)  NOT NULL,
    line_total  NUMERIC(14,2) NOT NULL,
    line_order  INTEGER       NOT NULL
);

CREATE INDEX idx_invoice_lines_invoice_id ON invoice_lines (invoice_id);

-- Compteur de numerotation deja utilise par les factures de demonstration ci-dessous.
INSERT INTO document_number_counters (doc_type, year, last_value) VALUES ('INVOICE', 2026, 2);

INSERT INTO invoices (number, client_id, client_name, issue_date, due_date, amount_excl_tax, tax_amount, total_amount, status, notes, created_by, updated_by)
SELECT 'FAC-2026-0001', id, name, DATE '2026-05-01', DATE '2026-05-31', 900000, 162000, 1062000, 'PAID', NULL, 'system', 'system'
FROM clients WHERE email = 'contact@acme.sn';

INSERT INTO invoices (number, client_id, client_name, issue_date, due_date, amount_excl_tax, tax_amount, total_amount, status, notes, created_by, updated_by)
SELECT 'FAC-2026-0002', id, name, DATE '2026-06-01', DATE '2026-06-30', 500000, 90000, 590000, 'SENT', 'Reglement attendu sous 30 jours.', 'system', 'system'
FROM clients WHERE email = 'contact@baobab-distribution.sn';

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, tax_rate, line_total, line_order)
SELECT id, 'Prestation de conseil - avril', 1, 900000, 18, 900000, 0 FROM invoices WHERE number = 'FAC-2026-0001';

INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, tax_rate, line_total, line_order)
SELECT id, 'Fourniture et livraison', 1, 500000, 18, 500000, 0 FROM invoices WHERE number = 'FAC-2026-0002';
