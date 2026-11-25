ALTER TABLE purchase_entry ADD COLUMN payment_type TEXT NOT NULL DEFAULT 'CREDIT' CHECK (payment_type IN ('CASH', 'CREDIT'));

CREATE TABLE supplier_payment (
    id TEXT PRIMARY KEY,
    supplier_id TEXT NOT NULL REFERENCES party(id),
    payment_date TEXT NOT NULL,
    amount_minor INTEGER NOT NULL CHECK (amount_minor > 0),
    payment_method TEXT NOT NULL CHECK (payment_method IN ('CASH', 'BANK', 'UPI')),
    reference_number TEXT,
    notes TEXT,
    created_at TEXT NOT NULL
) STRICT;

CREATE TABLE expense_entry (
    id TEXT PRIMARY KEY,
    expense_date TEXT NOT NULL,
    expense_type TEXT NOT NULL CHECK (expense_type IN ('FREIGHT', 'SHIPPING', 'RENT', 'UTILITIES', 'OTHER')),
    description TEXT NOT NULL,
    supplier_id TEXT REFERENCES party(id),
    payment_type TEXT NOT NULL CHECK (payment_type IN ('CASH', 'CREDIT')),
    payment_method TEXT CHECK (payment_method IN ('CASH', 'BANK', 'UPI')),
    reference_number TEXT,
    notes TEXT,
    amount_minor INTEGER NOT NULL CHECK (amount_minor > 0),
    created_at TEXT NOT NULL,
    CHECK ((payment_type = 'CREDIT' AND payment_method IS NULL) OR (payment_type = 'CASH' AND payment_method IS NOT NULL))
) STRICT;

CREATE TABLE journal_entry (
    id TEXT PRIMARY KEY,
    entry_date TEXT NOT NULL,
    source_type TEXT NOT NULL,
    source_id TEXT NOT NULL,
    narration TEXT NOT NULL,
    created_at TEXT NOT NULL,
    UNIQUE(source_type, source_id)
) STRICT;

CREATE TABLE journal_line (
    id TEXT PRIMARY KEY,
    journal_entry_id TEXT NOT NULL REFERENCES journal_entry(id) ON DELETE CASCADE,
    account_code TEXT NOT NULL,
    account_name TEXT NOT NULL,
    debit_minor INTEGER NOT NULL DEFAULT 0 CHECK (debit_minor >= 0),
    credit_minor INTEGER NOT NULL DEFAULT 0 CHECK (credit_minor >= 0),
    CHECK ((debit_minor > 0 AND credit_minor = 0) OR (credit_minor > 0 AND debit_minor = 0))
) STRICT;

CREATE INDEX idx_supplier_payment_supplier_date ON supplier_payment(supplier_id, payment_date);
CREATE INDEX idx_expense_entry_date ON expense_entry(expense_date);
CREATE INDEX idx_journal_entry_date ON journal_entry(entry_date);
CREATE INDEX idx_journal_line_entry ON journal_line(journal_entry_id);
