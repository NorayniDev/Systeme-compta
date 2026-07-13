CREATE TABLE journal_entries (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_date   DATE           NOT NULL,
    reference    VARCHAR(50)    NOT NULL,
    account_code VARCHAR(20)    NOT NULL,
    account_name VARCHAR(255)   NOT NULL,
    label        VARCHAR(500)   NOT NULL,
    debit        NUMERIC(14,2)  NOT NULL DEFAULT 0,
    credit       NUMERIC(14,2)  NOT NULL DEFAULT 0,
    source       VARCHAR(20)    NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_journal_entries_account_code ON journal_entries (account_code);
CREATE INDEX idx_journal_entries_entry_date ON journal_entries (entry_date);
CREATE INDEX idx_journal_entries_reference ON journal_entries (reference);

-- Ecritures de demonstration correspondant aux factures/paiements deja seedes (V9/V10) :
-- inserees directement en SQL car ces lignes existaient avant que JournalEntryRecorder
-- n'existe (les evenements de domaine ne se declenchent que sur de vraies transitions
-- applicatives, pas sur des lignes injectees par migration).

-- FAC-2026-0001 (ACME Senegal SARL, PAID) : facture + encaissement complet.
INSERT INTO journal_entries (entry_date, reference, account_code, account_name, label, debit, credit, source)
VALUES
    ('2026-05-01', 'FAC-2026-0001', '411', 'Clients', 'Facture FAC-2026-0001 - ACME Senegal SARL', 1062000, 0, 'INVOICE'),
    ('2026-05-01', 'FAC-2026-0001', '706', 'Prestations de services', 'Facture FAC-2026-0001 - ACME Senegal SARL', 0, 900000, 'INVOICE'),
    ('2026-05-01', 'FAC-2026-0001', '44571', 'TVA collectee', 'Facture FAC-2026-0001 - ACME Senegal SARL', 0, 162000, 'INVOICE'),
    ('2026-05-28', 'PAY-2026-0001', '512', 'Banque', 'Encaissement PAY-2026-0001 - ACME Senegal SARL', 1062000, 0, 'PAYMENT'),
    ('2026-05-28', 'PAY-2026-0001', '411', 'Clients', 'Encaissement PAY-2026-0001 - ACME Senegal SARL', 0, 1062000, 'PAYMENT');

-- FAC-2026-0002 (Baobab Distribution, SENT/OVERDUE) : facture emise, pas encore reglee.
INSERT INTO journal_entries (entry_date, reference, account_code, account_name, label, debit, credit, source)
VALUES
    ('2026-06-01', 'FAC-2026-0002', '411', 'Clients', 'Facture FAC-2026-0002 - Baobab Distribution', 590000, 0, 'INVOICE'),
    ('2026-06-01', 'FAC-2026-0002', '706', 'Prestations de services', 'Facture FAC-2026-0002 - Baobab Distribution', 0, 500000, 'INVOICE'),
    ('2026-06-01', 'FAC-2026-0002', '44571', 'TVA collectee', 'Facture FAC-2026-0002 - Baobab Distribution', 0, 90000, 'INVOICE');
