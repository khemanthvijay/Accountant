ALTER TABLE party ADD COLUMN ledger_type TEXT NOT NULL DEFAULT 'CUSTOMER';

UPDATE party
SET ledger_type = party_type
WHERE party_type IN ('CUSTOMER', 'SUPPLIER');

CREATE INDEX idx_party_ledger_type_name ON party(ledger_type, name);
