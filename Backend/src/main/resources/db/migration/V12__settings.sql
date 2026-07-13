CREATE TABLE company_settings (
    id                UUID PRIMARY KEY,
    company_name      VARCHAR(255)  NOT NULL,
    address           VARCHAR(500)  NOT NULL,
    tax_id            VARCHAR(100)  NOT NULL,
    currency          VARCHAR(10)   NOT NULL,
    default_tax_rate  NUMERIC(5, 2) NOT NULL,
    invoice_prefix    VARCHAR(20)   NOT NULL,
    quote_prefix      VARCHAR(20)   NOT NULL,
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_by        VARCHAR(255)
);

-- Ligne singleton unique : id fixe et connu, jamais expose via l'API (voir CompanySettingsService).
INSERT INTO company_settings
    (id, company_name, address, tax_id, currency, default_tax_rate, invoice_prefix, quote_prefix, updated_by)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'FacturationPME SARL', 'Rue 12, Plateau, Dakar, Senegal',
     'NINEA0011223', 'XOF', 18.00, 'FAC', 'DEV', 'system');
