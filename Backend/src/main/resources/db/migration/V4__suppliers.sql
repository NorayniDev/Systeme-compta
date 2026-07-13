CREATE TABLE suppliers (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name             VARCHAR(255)   NOT NULL,
    email            VARCHAR(255)   NOT NULL,
    phone            VARCHAR(50),
    address          VARCHAR(500),
    tax_id           VARCHAR(100),
    status           VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    total_purchased  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by       VARCHAR(255),
    updated_by       VARCHAR(255),
    CONSTRAINT uk_suppliers_email UNIQUE (email)
);

CREATE INDEX idx_suppliers_name ON suppliers (name);
CREATE INDEX idx_suppliers_status ON suppliers (status);

INSERT INTO suppliers (name, email, phone, address, tax_id, status, total_purchased, created_by, updated_by)
VALUES
    ('Sahel Fournitures SARL', 'contact@sahel-fournitures.sn', '+221 78 111 22 33', 'Rufisque, Senegal', 'NINEA0055667', 'ACTIVE', 2350000, 'system', 'system'),
    ('Dakar Papeterie SA', 'contact@dakar-papeterie.sn', '+221 78 222 33 44', 'Plateau, Dakar, Senegal', 'NINEA0066778', 'ACTIVE', 875000, 'system', 'system'),
    ('Import Export Teranga', 'contact@ie-teranga.sn', '+221 78 333 44 55', 'Port de Dakar, Senegal', 'NINEA0077889', 'ACTIVE', 4120000, 'system', 'system');
