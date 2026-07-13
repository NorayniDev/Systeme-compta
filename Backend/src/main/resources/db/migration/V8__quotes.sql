CREATE TABLE quotes (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    number                VARCHAR(50)    NOT NULL,
    client_id             UUID           NOT NULL REFERENCES clients (id) ON DELETE RESTRICT,
    client_name           VARCHAR(255)   NOT NULL,
    issue_date            DATE           NOT NULL,
    valid_until           DATE           NOT NULL,
    amount_excl_tax       NUMERIC(14, 2) NOT NULL DEFAULT 0,
    tax_amount            NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_amount          NUMERIC(14, 2) NOT NULL DEFAULT 0,
    status                VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    notes                 VARCHAR(2000),
    converted_invoice_id  UUID,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by            VARCHAR(255),
    updated_by            VARCHAR(255),
    CONSTRAINT uk_quotes_number UNIQUE (number)
);

CREATE INDEX idx_quotes_client_id ON quotes (client_id);
CREATE INDEX idx_quotes_status ON quotes (status);

CREATE TABLE quote_lines (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quote_id    UUID          NOT NULL REFERENCES quotes (id) ON DELETE CASCADE,
    description VARCHAR(500)  NOT NULL,
    quantity    NUMERIC(12,2) NOT NULL,
    unit_price  NUMERIC(14,2) NOT NULL,
    tax_rate    NUMERIC(5,2)  NOT NULL,
    line_total  NUMERIC(14,2) NOT NULL,
    line_order  INTEGER       NOT NULL
);

CREATE INDEX idx_quote_lines_quote_id ON quote_lines (quote_id);

-- Compteur de numerotation deja utilise par les devis de demonstration ci-dessous.
INSERT INTO document_number_counters (doc_type, year, last_value) VALUES ('QUOTE', 2026, 2);

INSERT INTO quotes (number, client_id, client_name, issue_date, valid_until, amount_excl_tax, tax_amount, total_amount, status, notes, created_by, updated_by)
SELECT 'DEV-2026-0001', id, name, DATE '2026-06-15', DATE '2026-07-15', 1200000, 216000, 1416000, 'SENT', 'Devis pour renouvellement du parc informatique.', 'system', 'system'
FROM clients WHERE email = 'contact@acme.sn';

INSERT INTO quotes (number, client_id, client_name, issue_date, valid_until, amount_excl_tax, tax_amount, total_amount, status, notes, created_by, updated_by)
SELECT 'DEV-2026-0002', id, name, DATE '2026-06-20', DATE '2026-07-20', 600000, 108000, 708000, 'ACCEPTED', NULL, 'system', 'system'
FROM clients WHERE email = 'contact@casamance-fruits.sn';

INSERT INTO quote_lines (quote_id, description, quantity, unit_price, tax_rate, line_total, line_order)
SELECT id, 'Ordinateur portable professionnel', 4, 250000, 18, 1000000, 0 FROM quotes WHERE number = 'DEV-2026-0001';
INSERT INTO quote_lines (quote_id, description, quantity, unit_price, tax_rate, line_total, line_order)
SELECT id, 'Installation et configuration', 1, 200000, 18, 200000, 1 FROM quotes WHERE number = 'DEV-2026-0001';

INSERT INTO quote_lines (quote_id, description, quantity, unit_price, tax_rate, line_total, line_order)
SELECT id, 'Prestation de conseil - 3 jours', 3, 200000, 18, 600000, 0 FROM quotes WHERE number = 'DEV-2026-0002';
