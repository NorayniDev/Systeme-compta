CREATE TABLE products (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255)   NOT NULL,
    sku             VARCHAR(100)   NOT NULL,
    description     VARCHAR(1000),
    category        VARCHAR(255),
    unit_price      NUMERIC(14, 2) NOT NULL DEFAULT 0,
    tax_rate        NUMERIC(5, 2)  NOT NULL DEFAULT 0,
    unit            VARCHAR(20)    NOT NULL,
    stock_quantity  INTEGER        NOT NULL DEFAULT 0,
    status          VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    CONSTRAINT uk_products_sku UNIQUE (sku)
);

CREATE INDEX idx_products_name ON products (name);
CREATE INDEX idx_products_status ON products (status);
CREATE INDEX idx_products_category ON products (category);

INSERT INTO products (name, sku, description, category, unit_price, tax_rate, unit, stock_quantity, status, created_by, updated_by)
VALUES
    ('Ramette de papier A4', 'PROD-0001', 'Ramette de 500 feuilles A4 80g', 'Fournitures de bureau', 3500, 18, 'BOX', 240, 'ACTIVE', 'system', 'system'),
    ('Cartouche encre HP 305', 'PROD-0002', 'Cartouche encre noire HP 305', 'Consommables informatiques', 12500, 18, 'PIECE', 35, 'ACTIVE', 'system', 'system'),
    ('Sac de riz brise 25kg', 'PROD-0003', 'Sac de riz brise premium 25kg', 'Alimentaire', 14000, 18, 'KG', 80, 'ACTIVE', 'system', 'system'),
    ('Bidon huile vegetale 5L', 'PROD-0004', 'Bidon huile vegetale raffinee 5 litres', 'Alimentaire', 6500, 18, 'LITER', 5, 'ACTIVE', 'system', 'system');
