-- V12__add_procurement.sql
-- Migration to add procurement tables for purchase requisitions, orders, quotes, and files
-- Author: myRC Team
-- Version: 1.0.0
-- Date: 2026-01-28

-- Procurement Items table
CREATE TABLE procurement_items (
    id BIGSERIAL PRIMARY KEY,
    fiscal_year_id BIGINT NOT NULL REFERENCES fiscal_years(id) ON DELETE CASCADE,
    purchase_requisition VARCHAR(100) NOT NULL,
    purchase_order VARCHAR(100),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_date TIMESTAMP NOT NULL DEFAULT NOW(),
    modified_date TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_procurement_pr_fy UNIQUE (fiscal_year_id, purchase_requisition)
);

-- Index for fast lookups by fiscal year
CREATE INDEX idx_procurement_items_fy ON procurement_items(fiscal_year_id);

-- Index for searching by PR or PO
CREATE INDEX idx_procurement_items_pr ON procurement_items(purchase_requisition);
CREATE INDEX idx_procurement_items_po ON procurement_items(purchase_order);

-- Procurement Quotes table
CREATE TABLE procurement_quotes (
    id BIGSERIAL PRIMARY KEY,
    procurement_item_id BIGINT NOT NULL REFERENCES procurement_items(id) ON DELETE CASCADE,
    vendor_name VARCHAR(255) NOT NULL,
    vendor_contact VARCHAR(255),
    quote_reference VARCHAR(100),
    amount DECIMAL(19, 4),
    currency VARCHAR(10) DEFAULT 'CAD',
    received_date DATE,
    expiry_date DATE,
    notes TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    selected BOOLEAN NOT NULL DEFAULT FALSE,
    created_date TIMESTAMP NOT NULL DEFAULT NOW(),
    modified_date TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Index for fast lookups by procurement item
CREATE INDEX idx_procurement_quotes_item ON procurement_quotes(procurement_item_id);

-- Procurement Quote Files table
CREATE TABLE procurement_quote_files (
    id BIGSERIAL PRIMARY KEY,
    quote_id BIGINT NOT NULL REFERENCES procurement_quotes(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content BYTEA NOT NULL,
    description TEXT,
    uploaded_date TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Index for fast lookups by quote
CREATE INDEX idx_procurement_quote_files_quote ON procurement_quote_files(quote_id);

-- Comments
COMMENT ON TABLE procurement_items IS 'Stores procurement items with PR, PO, and status tracking';
COMMENT ON TABLE procurement_quotes IS 'Stores vendor quotes for procurement items';
COMMENT ON TABLE procurement_quote_files IS 'Stores files attached to procurement quotes';

COMMENT ON COLUMN procurement_items.purchase_requisition IS 'Purchase Requisition number';
COMMENT ON COLUMN procurement_items.purchase_order IS 'Purchase Order number (assigned after approval)';
COMMENT ON COLUMN procurement_items.status IS 'Status: DRAFT, PENDING_QUOTES, QUOTES_RECEIVED, UNDER_REVIEW, APPROVED, PO_ISSUED, COMPLETED, CANCELLED';
COMMENT ON COLUMN procurement_quotes.selected IS 'Whether this quote was selected as the winning quote';
COMMENT ON COLUMN procurement_quotes.status IS 'Status: PENDING, SELECTED, REJECTED, EXPIRED';
COMMENT ON COLUMN procurement_quote_files.content IS 'Binary file content stored as BYTEA';
