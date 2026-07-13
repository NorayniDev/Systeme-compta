CREATE TABLE service_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255)   NOT NULL,
    code        VARCHAR(100)   NOT NULL,
    description VARCHAR(1000),
    category    VARCHAR(255),
    unit_price  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    tax_rate    NUMERIC(5, 2)  NOT NULL DEFAULT 0,
    unit        VARCHAR(20)    NOT NULL,
    status      VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    CONSTRAINT uk_service_items_code UNIQUE (code)
);

CREATE INDEX idx_service_items_name ON service_items (name);
CREATE INDEX idx_service_items_status ON service_items (status);
CREATE INDEX idx_service_items_category ON service_items (category);

INSERT INTO service_items (name, code, description, category, unit_price, tax_rate, unit, status, created_by, updated_by)
VALUES
    ('Consultation comptable', 'SERV-0001', 'Consultation comptable et fiscale', 'Conseil', 25000, 18, 'HOUR', 'ACTIVE', 'system', 'system'),
    ('Audit financier', 'SERV-0002', 'Audit financier annuel complet', 'Audit', 850000, 18, 'FLAT_RATE', 'ACTIVE', 'system', 'system'),
    ('Maintenance informatique', 'SERV-0003', 'Maintenance parc informatique mensuelle', 'IT', 150000, 18, 'MONTH', 'ACTIVE', 'system', 'system'),
    ('Formation Excel avancee', 'SERV-0004', 'Formation Excel avancee sur site', 'Formation', 300000, 18, 'DAY', 'ACTIVE', 'system', 'system');
