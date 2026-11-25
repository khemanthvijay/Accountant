CREATE TABLE customer_receipt (
    id TEXT PRIMARY KEY,
    customer_id TEXT NOT NULL REFERENCES party(id),
    receipt_date TEXT NOT NULL,
    amount_minor INTEGER NOT NULL CHECK (amount_minor > 0),
    payment_method TEXT NOT NULL CHECK (payment_method IN ('CASH', 'BANK', 'UPI')),
    reference_number TEXT,
    notes TEXT,
    created_at TEXT NOT NULL
) STRICT;

CREATE INDEX idx_customer_receipt_customer_date ON customer_receipt(customer_id, receipt_date);
