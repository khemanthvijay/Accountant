ALTER TABLE sales_entry ADD COLUMN payment_type TEXT NOT NULL DEFAULT 'CREDIT'
    CHECK (payment_type IN ('CASH', 'CREDIT'));
