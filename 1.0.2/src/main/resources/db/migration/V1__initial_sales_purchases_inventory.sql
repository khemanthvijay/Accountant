CREATE TABLE inventory_item (
    id TEXT PRIMARY KEY,
    sku TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    unit TEXT NOT NULL,
    opening_quantity_scaled INTEGER NOT NULL DEFAULT 0 CHECK (opening_quantity_scaled >= 0),
    reorder_level_scaled INTEGER NOT NULL DEFAULT 0 CHECK (reorder_level_scaled >= 0),
    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    created_at TEXT NOT NULL
) STRICT;

CREATE TABLE purchase_entry (
    id TEXT PRIMARY KEY,
    entry_date TEXT NOT NULL,
    supplier_name TEXT NOT NULL,
    supplier_invoice_number TEXT,
    notes TEXT,
    total_minor INTEGER NOT NULL CHECK (total_minor >= 0),
    created_at TEXT NOT NULL
) STRICT;

CREATE TABLE purchase_entry_line (
    id TEXT PRIMARY KEY,
    purchase_entry_id TEXT NOT NULL REFERENCES purchase_entry(id),
    item_id TEXT NOT NULL REFERENCES inventory_item(id),
    item_name_snapshot TEXT NOT NULL,
    quantity_scaled INTEGER NOT NULL CHECK (quantity_scaled > 0),
    unit_cost_minor INTEGER NOT NULL CHECK (unit_cost_minor >= 0),
    line_total_minor INTEGER NOT NULL CHECK (line_total_minor >= 0)
) STRICT;

CREATE TABLE sales_entry (
    id TEXT PRIMARY KEY,
    entry_date TEXT NOT NULL,
    customer_name TEXT NOT NULL,
    invoice_number TEXT,
    notes TEXT,
    total_minor INTEGER NOT NULL CHECK (total_minor >= 0),
    created_at TEXT NOT NULL
) STRICT;

CREATE TABLE sales_entry_line (
    id TEXT PRIMARY KEY,
    sales_entry_id TEXT NOT NULL REFERENCES sales_entry(id),
    item_id TEXT NOT NULL REFERENCES inventory_item(id),
    item_name_snapshot TEXT NOT NULL,
    quantity_scaled INTEGER NOT NULL CHECK (quantity_scaled > 0),
    unit_price_minor INTEGER NOT NULL CHECK (unit_price_minor >= 0),
    line_total_minor INTEGER NOT NULL CHECK (line_total_minor >= 0)
) STRICT;

CREATE TABLE stock_movement (
    id TEXT PRIMARY KEY,
    item_id TEXT NOT NULL REFERENCES inventory_item(id),
    movement_date TEXT NOT NULL,
    movement_type TEXT NOT NULL CHECK (movement_type IN ('OPENING', 'PURCHASE', 'SALE')),
    quantity_delta_scaled INTEGER NOT NULL,
    source_type TEXT NOT NULL,
    source_id TEXT NOT NULL,
    created_at TEXT NOT NULL
) STRICT;

CREATE INDEX idx_stock_movement_item_date ON stock_movement(item_id, movement_date);
CREATE INDEX idx_purchase_entry_date ON purchase_entry(entry_date);
CREATE INDEX idx_sales_entry_date ON sales_entry(entry_date);
