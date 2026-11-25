ALTER TABLE inventory_item ADD COLUMN description TEXT;
ALTER TABLE inventory_item ADD COLUMN alias TEXT;
ALTER TABLE inventory_item ADD COLUMN category TEXT;
ALTER TABLE inventory_item ADD COLUMN opening_rate_minor INTEGER NOT NULL DEFAULT 0 CHECK (opening_rate_minor >= 0);
ALTER TABLE inventory_item ADD COLUMN default_purchase_rate_minor INTEGER NOT NULL DEFAULT 0 CHECK (default_purchase_rate_minor >= 0);
ALTER TABLE inventory_item ADD COLUMN default_sales_rate_minor INTEGER NOT NULL DEFAULT 0 CHECK (default_sales_rate_minor >= 0);

CREATE TABLE party (
    id TEXT PRIMARY KEY,
    party_type TEXT NOT NULL CHECK (party_type IN ('SUPPLIER', 'CUSTOMER')),
    name TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    address TEXT,
    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    created_at TEXT NOT NULL,
    UNIQUE (party_type, name)
) STRICT;

ALTER TABLE purchase_entry ADD COLUMN supplier_id TEXT;
ALTER TABLE sales_entry ADD COLUMN customer_id TEXT;

CREATE INDEX idx_party_type_name ON party(party_type, name);
CREATE INDEX idx_purchase_entry_supplier ON purchase_entry(supplier_id);
CREATE INDEX idx_sales_entry_customer ON sales_entry(customer_id);
