CREATE TABLE pricing_group (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    created_at TEXT NOT NULL
) STRICT;

CREATE TABLE pricing_group_item (
    pricing_group_id TEXT NOT NULL REFERENCES pricing_group(id) ON DELETE CASCADE,
    item_id TEXT NOT NULL UNIQUE REFERENCES inventory_item(id),
    PRIMARY KEY (pricing_group_id, item_id)
) STRICT;

CREATE TABLE pricing_payment_adjustment (
    pricing_group_id TEXT NOT NULL REFERENCES pricing_group(id) ON DELETE CASCADE,
    payment_type TEXT NOT NULL CHECK (payment_type IN ('CASH', 'BANK', 'CREDIT')),
    adjustment_basis_points INTEGER NOT NULL,
    PRIMARY KEY (pricing_group_id, payment_type)
) STRICT;

CREATE TABLE pricing_quantity_adjustment (
    id TEXT PRIMARY KEY,
    pricing_group_id TEXT NOT NULL REFERENCES pricing_group(id) ON DELETE CASCADE,
    minimum_quantity_scaled INTEGER NOT NULL CHECK (minimum_quantity_scaled > 0),
    maximum_quantity_scaled INTEGER CHECK (maximum_quantity_scaled IS NULL OR maximum_quantity_scaled >= minimum_quantity_scaled),
    adjustment_basis_points INTEGER NOT NULL
) STRICT;

ALTER TABLE sales_entry ADD COLUMN payment_type_v2 TEXT NOT NULL DEFAULT 'CASH' CHECK (payment_type_v2 IN ('CASH', 'BANK', 'CREDIT'));

CREATE INDEX idx_pricing_group_item_item ON pricing_group_item(item_id);
CREATE INDEX idx_pricing_quantity_group_minimum ON pricing_quantity_adjustment(pricing_group_id, minimum_quantity_scaled);
