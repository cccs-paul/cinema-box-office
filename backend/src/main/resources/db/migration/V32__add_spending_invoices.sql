-- V32: Add spending invoices and spending invoice files tables
-- Invoices/receipts can be attached to spending items
-- Each invoice can have 0..n file attachments

-- Spending Invoices table
CREATE TABLE IF NOT EXISTS spending_invoices (
    id BIGSERIAL PRIMARY KEY,
    spending_item_id BIGINT NOT NULL REFERENCES spending_items(id) ON DELETE CASCADE,
    date_received DATE,
    date_processed DATE,
    comments VARCHAR(2000),
    amount NUMERIC(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'CAD',
    exchange_rate NUMERIC(15,6),
    amount_cad NUMERIC(15,2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100)
);

-- Spending Invoice Files table
CREATE TABLE IF NOT EXISTS spending_invoice_files (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES spending_invoices(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    content BYTEA NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_spending_invoices_spending_item_id ON spending_invoices(spending_item_id);
CREATE INDEX IF NOT EXISTS idx_spending_invoice_files_invoice_id ON spending_invoice_files(invoice_id);
