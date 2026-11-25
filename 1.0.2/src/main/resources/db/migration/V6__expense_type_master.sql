CREATE TABLE expense_type_master (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    account_code TEXT NOT NULL UNIQUE,
    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    created_at TEXT NOT NULL
) STRICT;

INSERT INTO expense_type_master (id, name, account_code, created_at) VALUES
    ('freight', 'Freight', 'FREIGHT', '1970-01-01T00:00:00Z'),
    ('shipping', 'Shipping', 'SHIPPING', '1970-01-01T00:00:00Z'),
    ('rent', 'Rent', 'RENT', '1970-01-01T00:00:00Z'),
    ('utilities', 'Utilities', 'UTILITIES', '1970-01-01T00:00:00Z'),
    ('other', 'Other', 'OTHER', '1970-01-01T00:00:00Z');

ALTER TABLE expense_entry ADD COLUMN expense_type_id TEXT REFERENCES expense_type_master(id);
UPDATE expense_entry SET expense_type_id = LOWER(expense_type) WHERE expense_type_id IS NULL;
CREATE INDEX idx_expense_entry_type ON expense_entry(expense_type_id);
