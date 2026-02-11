-- myRC - Complete Database Schema
-- Copyright (c) 2026 myRC Team
-- Licensed under MIT License
--
-- Description:
-- Creates the complete database schema for the myRC application.
-- All tables are created in their final state with proper constraints,
-- foreign keys, indexes, and triggers.
--
-- Uses CREATE TABLE IF NOT EXISTS throughout to be safe for existing databases
-- where Flyway baseline-on-migrate may run this migration against a pre-existing schema.
--
-- Table creation order respects foreign key dependencies:
--   1. users (no deps)
--   2. user_roles (→ users)
--   3. responsibility_centres (→ users)
--   4. rc_access (→ responsibility_centres, users)
--   5. fiscal_years (→ responsibility_centres)
--   6. monies (→ fiscal_years)
--   7. categories (→ fiscal_years)
--   8. spending_categories (→ fiscal_years) [legacy]
--   9. funding_items (→ fiscal_years, categories)
--  10. money_allocations (→ funding_items, monies)
--  11. spending_items (→ fiscal_years, categories, procurement_items)
--  12. spending_money_allocations (→ spending_items, monies)
--  13. spending_events (→ spending_items)
--  14. spending_invoices (→ spending_items)
--  15. spending_invoice_files (→ spending_invoices)
--  16. procurement_items (→ fiscal_years, categories)
--  17. procurement_events (→ procurement_items)
--  18. procurement_event_files (→ procurement_events)
--  19. procurement_quotes (→ procurement_items)
--  20. procurement_quote_files (→ procurement_quotes)
--  21. audit_events (no deps)

-- ============================================================================
-- 1. USERS
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    full_name VARCHAR(100),
    password_hash VARCHAR(255),
    auth_provider VARCHAR(20) NOT NULL,
    external_id VARCHAR(255),
    oauth_provider VARCHAR(50),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    account_locked_until TIMESTAMP,
    profile_description VARCHAR(500),
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    password_changed_at TIMESTAMP,
    theme VARCHAR(20) NOT NULL DEFAULT 'light',
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX IF NOT EXISTS idx_users_username_lower ON users (LOWER(username));
CREATE INDEX IF NOT EXISTS idx_users_email_lower ON users (LOWER(email));
CREATE INDEX IF NOT EXISTS idx_users_external_id ON users (external_id) WHERE external_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_auth_provider ON users (auth_provider);
CREATE INDEX IF NOT EXISTS idx_users_enabled ON users (enabled);

-- ============================================================================
-- 2. USER_ROLES (ElementCollection table for User.roles)
-- ============================================================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);

-- ============================================================================
-- 3. RESPONSIBILITY CENTRES
-- ============================================================================
CREATE TABLE IF NOT EXISTS responsibility_centres (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_responsibility_centres_name UNIQUE (name)
);

CREATE INDEX IF NOT EXISTS idx_responsibility_centres_owner_id ON responsibility_centres(owner_id);
CREATE INDEX IF NOT EXISTS idx_responsibility_centres_active ON responsibility_centres (active);
CREATE INDEX IF NOT EXISTS idx_responsibility_centres_owner_active ON responsibility_centres (owner_id, active);

-- ============================================================================
-- 4. RC_ACCESS
-- ============================================================================
CREATE TABLE IF NOT EXISTS rc_access (
    id BIGSERIAL PRIMARY KEY,
    responsibility_centre_id BIGINT NOT NULL REFERENCES responsibility_centres(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    principal_identifier VARCHAR(255),
    principal_display_name VARCHAR(255),
    principal_type VARCHAR(255) NOT NULL DEFAULT 'USER',
    access_level VARCHAR(255) NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_rc_access_responsibility_centre_id ON rc_access(responsibility_centre_id);
CREATE INDEX IF NOT EXISTS idx_rc_access_user_id ON rc_access(user_id);
CREATE INDEX IF NOT EXISTS idx_rc_access_principal_identifier ON rc_access(principal_identifier);
CREATE UNIQUE INDEX IF NOT EXISTS idx_rc_access_unique_principal
    ON rc_access (responsibility_centre_id, principal_identifier, principal_type)
    WHERE user_id IS NULL;
CREATE INDEX IF NOT EXISTS idx_rc_access_rc_access_level ON rc_access (responsibility_centre_id, access_level);
CREATE INDEX IF NOT EXISTS idx_rc_access_principal_id_type ON rc_access (principal_identifier, principal_type);

-- ============================================================================
-- 5. FISCAL YEARS
-- ============================================================================
CREATE TABLE IF NOT EXISTS fiscal_years (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    responsibility_centre_id BIGINT NOT NULL REFERENCES responsibility_centres(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    show_search_box BOOLEAN NOT NULL DEFAULT TRUE,
    show_category_filter BOOLEAN NOT NULL DEFAULT TRUE,
    group_by_category BOOLEAN NOT NULL DEFAULT FALSE,
    on_target_min INTEGER NOT NULL DEFAULT -2,
    on_target_max INTEGER NOT NULL DEFAULT 2,
    CONSTRAINT uk_fy_name_rc UNIQUE (name, responsibility_centre_id),
    CONSTRAINT chk_on_target_min CHECK (on_target_min >= -100 AND on_target_min <= 100),
    CONSTRAINT chk_on_target_max CHECK (on_target_max >= -100 AND on_target_max <= 100),
    CONSTRAINT chk_on_target_range CHECK (on_target_min <= on_target_max)
);

CREATE INDEX IF NOT EXISTS idx_fiscal_years_responsibility_centre_id ON fiscal_years(responsibility_centre_id);
CREATE INDEX IF NOT EXISTS idx_fiscal_years_rc_active ON fiscal_years (responsibility_centre_id, active);

-- ============================================================================
-- 6. MONIES
-- ============================================================================
CREATE TABLE IF NOT EXISTS monies (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    fiscal_year_id BIGINT NOT NULL REFERENCES fiscal_years(id) ON DELETE CASCADE,
    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_money_code_fy UNIQUE(code, fiscal_year_id)
);

CREATE INDEX IF NOT EXISTS idx_monies_fiscal_year_id ON monies(fiscal_year_id);
CREATE INDEX IF NOT EXISTS idx_monies_code ON monies(code);
CREATE INDEX IF NOT EXISTS idx_monies_is_default ON monies(is_default);
CREATE INDEX IF NOT EXISTS idx_monies_active ON monies(active);
CREATE INDEX IF NOT EXISTS idx_monies_display_order ON monies(display_order);
CREATE INDEX IF NOT EXISTS idx_monies_fy_active_order ON monies (fiscal_year_id, active, display_order);

-- Trigger: auto-create default A-Base money when a fiscal year is inserted
CREATE OR REPLACE FUNCTION ensure_default_money()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO monies (code, name, description, is_default, fiscal_year_id, display_order)
    VALUES ('AB', 'A-Base', 'Default A-Base funding allocation', TRUE, NEW.id, 0);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = 'trg_create_default_money'
    ) THEN
        CREATE TRIGGER trg_create_default_money
            AFTER INSERT ON fiscal_years
            FOR EACH ROW
            EXECUTE FUNCTION ensure_default_money();
    END IF;
END $$;

-- ============================================================================
-- 7. CATEGORIES
-- ============================================================================
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,
    funding_type VARCHAR(20) NOT NULL DEFAULT 'BOTH',
    translation_key VARCHAR(100),
    fiscal_year_id BIGINT NOT NULL REFERENCES fiscal_years(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_category_name_fy UNIQUE (name, fiscal_year_id)
);

CREATE INDEX IF NOT EXISTS idx_categories_fiscal_year_id ON categories(fiscal_year_id);
CREATE INDEX IF NOT EXISTS idx_categories_display_order ON categories(display_order);
CREATE INDEX IF NOT EXISTS idx_categories_is_default ON categories(is_default);
CREATE INDEX IF NOT EXISTS idx_categories_funding_type ON categories(funding_type);
CREATE INDEX IF NOT EXISTS idx_categories_fy_active_order ON categories (fiscal_year_id, active, display_order);

-- ============================================================================
-- 8. SPENDING CATEGORIES (legacy table — data migrated to categories)
-- ============================================================================
CREATE TABLE IF NOT EXISTS spending_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,
    fiscal_year_id BIGINT NOT NULL REFERENCES fiscal_years(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_spending_category_name_fy UNIQUE (name, fiscal_year_id)
);

-- ============================================================================
-- 9. FUNDING ITEMS
-- ============================================================================
CREATE TABLE IF NOT EXISTS funding_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    source VARCHAR(20),
    source_details VARCHAR(2000),
    currency VARCHAR(3) NOT NULL DEFAULT 'CAD',
    exchange_rate NUMERIC(15,6),
    fiscal_year_id BIGINT NOT NULL REFERENCES fiscal_years(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_fi_name_fy UNIQUE (name, fiscal_year_id),
    CONSTRAINT valid_currency CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT positive_exchange_rate CHECK (exchange_rate IS NULL OR exchange_rate > 0)
);

CREATE INDEX IF NOT EXISTS idx_funding_items_fiscal_year_id ON funding_items(fiscal_year_id);
CREATE INDEX IF NOT EXISTS idx_funding_items_category_id ON funding_items(category_id);
CREATE INDEX IF NOT EXISTS idx_funding_items_fy_active ON funding_items (fiscal_year_id, active);
CREATE INDEX IF NOT EXISTS idx_funding_items_fy_category_active ON funding_items (fiscal_year_id, category_id, active);
CREATE INDEX IF NOT EXISTS idx_funding_items_name_fy ON funding_items (fiscal_year_id, name);

-- ============================================================================
-- 10. MONEY ALLOCATIONS
-- ============================================================================
CREATE TABLE IF NOT EXISTS money_allocations (
    id BIGSERIAL PRIMARY KEY,
    funding_item_id BIGINT NOT NULL REFERENCES funding_items(id) ON DELETE CASCADE,
    money_id BIGINT NOT NULL REFERENCES monies(id) ON DELETE CASCADE,
    cap_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    om_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_allocation_fi_money UNIQUE (funding_item_id, money_id)
);

CREATE INDEX IF NOT EXISTS idx_money_allocations_funding_item_id ON money_allocations (funding_item_id);
CREATE INDEX IF NOT EXISTS idx_money_allocations_money_id ON money_allocations (money_id);
CREATE INDEX IF NOT EXISTS idx_money_allocations_fi_money ON money_allocations (funding_item_id, money_id);

-- ============================================================================
-- 11. PROCUREMENT ITEMS (created before spending_items due to FK dependency)
-- ============================================================================
CREATE TABLE IF NOT EXISTS procurement_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_requisition VARCHAR(100),
    purchase_order VARCHAR(100),
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    preferred_vendor VARCHAR(200),
    contract_number VARCHAR(100),
    contract_start_date DATE,
    contract_end_date DATE,
    final_price NUMERIC(15,2),
    final_price_currency VARCHAR(3),
    final_price_exchange_rate NUMERIC(10,6),
    final_price_cad NUMERIC(15,2),
    quoted_price NUMERIC(15,2),
    quoted_price_currency VARCHAR(3),
    quoted_price_exchange_rate NUMERIC(10,6),
    quoted_price_cad NUMERIC(15,2),
    procurement_completed BOOLEAN,
    procurement_completed_date DATE,
    tracking_status VARCHAR(20),
    procurement_type VARCHAR(20),
    fiscal_year_id BIGINT NOT NULL REFERENCES fiscal_years(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_procurement_items_fy ON procurement_items(fiscal_year_id);
CREATE INDEX IF NOT EXISTS idx_procurement_items_category ON procurement_items(category_id);
CREATE INDEX IF NOT EXISTS idx_procurement_items_fy_active ON procurement_items (fiscal_year_id, active);
CREATE INDEX IF NOT EXISTS idx_procurement_items_fy_category_active ON procurement_items (fiscal_year_id, category_id, active);
CREATE INDEX IF NOT EXISTS idx_procurement_items_name_fy ON procurement_items (fiscal_year_id, name);
CREATE UNIQUE INDEX IF NOT EXISTS uk_pi_pr_fy_partial
    ON procurement_items (purchase_requisition, fiscal_year_id)
    WHERE purchase_requisition IS NOT NULL AND purchase_requisition != '';

-- ============================================================================
-- 12. SPENDING ITEMS
-- ============================================================================
CREATE TABLE IF NOT EXISTS spending_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    vendor VARCHAR(200),
    reference_number VARCHAR(100),
    amount NUMERIC(15,2),
    eco_amount NUMERIC(15,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    currency VARCHAR(3) NOT NULL DEFAULT 'CAD',
    exchange_rate NUMERIC(15,6),
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE SET NULL,
    fiscal_year_id BIGINT NOT NULL REFERENCES fiscal_years(id) ON DELETE CASCADE,
    procurement_item_id BIGINT REFERENCES procurement_items(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_si_name_fy UNIQUE (name, fiscal_year_id)
);

CREATE INDEX IF NOT EXISTS idx_spending_items_fiscal_year_id ON spending_items(fiscal_year_id);
CREATE INDEX IF NOT EXISTS idx_spending_items_category_id ON spending_items(category_id);
CREATE INDEX IF NOT EXISTS idx_spending_items_fy_active ON spending_items (fiscal_year_id, active);
CREATE INDEX IF NOT EXISTS idx_spending_items_fy_category_active ON spending_items (fiscal_year_id, category_id, active);
CREATE INDEX IF NOT EXISTS idx_spending_items_name_fy ON spending_items (fiscal_year_id, name);
CREATE INDEX IF NOT EXISTS idx_spending_items_category_active ON spending_items (category_id, active);

-- ============================================================================
-- 13. SPENDING MONEY ALLOCATIONS
-- ============================================================================
CREATE TABLE IF NOT EXISTS spending_money_allocations (
    id BIGSERIAL PRIMARY KEY,
    spending_item_id BIGINT NOT NULL REFERENCES spending_items(id) ON DELETE CASCADE,
    money_id BIGINT NOT NULL REFERENCES monies(id) ON DELETE CASCADE,
    cap_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    om_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_spending_allocation_si_money UNIQUE (spending_item_id, money_id)
);

CREATE INDEX IF NOT EXISTS idx_spending_money_alloc_spending_item_id ON spending_money_allocations (spending_item_id);
CREATE INDEX IF NOT EXISTS idx_spending_money_alloc_money_id ON spending_money_allocations (money_id);
CREATE INDEX IF NOT EXISTS idx_spending_money_alloc_si_money ON spending_money_allocations (spending_item_id, money_id);

-- ============================================================================
-- 14. SPENDING EVENTS
-- ============================================================================
CREATE TABLE IF NOT EXISTS spending_events (
    id BIGSERIAL PRIMARY KEY,
    spending_item_id BIGINT NOT NULL REFERENCES spending_items(id) ON DELETE CASCADE,
    event_type VARCHAR(35) NOT NULL DEFAULT 'PENDING',
    event_date DATE NOT NULL DEFAULT CURRENT_DATE,
    description VARCHAR(2000),
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_spending_events_item ON spending_events(spending_item_id);
CREATE INDEX IF NOT EXISTS idx_spending_events_item_active ON spending_events (spending_item_id, active);
CREATE INDEX IF NOT EXISTS idx_spending_events_date ON spending_events (event_date DESC);
CREATE INDEX IF NOT EXISTS idx_spending_events_type ON spending_events (event_type);

-- ============================================================================
-- 15. SPENDING INVOICES
-- ============================================================================
CREATE TABLE IF NOT EXISTS spending_invoices (
    id BIGSERIAL PRIMARY KEY,
    spending_item_id BIGINT NOT NULL REFERENCES spending_items(id) ON DELETE CASCADE,
    date_received DATE,
    date_processed DATE,
    description VARCHAR(2000),
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

CREATE INDEX IF NOT EXISTS idx_spending_invoices_spending_item_id ON spending_invoices(spending_item_id);
CREATE INDEX IF NOT EXISTS idx_spending_invoices_item_active ON spending_invoices (spending_item_id, active);

-- ============================================================================
-- 16. SPENDING INVOICE FILES
-- ============================================================================
CREATE TABLE IF NOT EXISTS spending_invoice_files (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES spending_invoices(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    data BYTEA NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_spending_invoice_files_invoice_id ON spending_invoice_files(invoice_id);
CREATE INDEX IF NOT EXISTS idx_spending_invoice_files_inv_active ON spending_invoice_files (invoice_id, active);

-- ============================================================================
-- 17. PROCUREMENT EVENTS
-- ============================================================================
CREATE TABLE IF NOT EXISTS procurement_events (
    id BIGSERIAL PRIMARY KEY,
    procurement_item_id BIGINT NOT NULL REFERENCES procurement_items(id) ON DELETE CASCADE,
    event_type VARCHAR(35) NOT NULL DEFAULT 'NOT_STARTED',
    event_date DATE NOT NULL,
    description VARCHAR(2000),
    old_status VARCHAR(30),
    new_status VARCHAR(30),
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_procurement_events_item_id ON procurement_events (procurement_item_id);
CREATE INDEX IF NOT EXISTS idx_procurement_events_item_active ON procurement_events (procurement_item_id, active);
CREATE INDEX IF NOT EXISTS idx_procurement_events_item_type_active ON procurement_events (procurement_item_id, event_type, active);
CREATE INDEX IF NOT EXISTS idx_procurement_events_item_status_active ON procurement_events (procurement_item_id, new_status, active);
CREATE INDEX IF NOT EXISTS idx_procurement_events_item_date ON procurement_events (procurement_item_id, event_date DESC);
CREATE INDEX IF NOT EXISTS idx_procurement_events_created_by ON procurement_events (created_by) WHERE active = TRUE;

-- ============================================================================
-- 18. PROCUREMENT EVENT FILES
-- ============================================================================
CREATE TABLE IF NOT EXISTS procurement_event_files (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    data BYTEA NOT NULL,
    description VARCHAR(500),
    event_id BIGINT NOT NULL REFERENCES procurement_events(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_event_files_event_id ON procurement_event_files(event_id);
CREATE INDEX IF NOT EXISTS idx_procurement_event_files_event_active ON procurement_event_files (event_id, active);

-- ============================================================================
-- 19. PROCUREMENT QUOTES
-- ============================================================================
CREATE TABLE IF NOT EXISTS procurement_quotes (
    id BIGSERIAL PRIMARY KEY,
    vendor_name VARCHAR(200) NOT NULL,
    vendor_contact VARCHAR(500),
    quote_reference VARCHAR(100),
    amount NUMERIC(15,2),
    amount_cap NUMERIC(15,2),
    amount_om NUMERIC(15,2),
    currency VARCHAR(3) NOT NULL DEFAULT 'CAD',
    exchange_rate NUMERIC(10,6),
    amount_cap_cad NUMERIC(15,2),
    amount_om_cad NUMERIC(15,2),
    received_date DATE,
    expiry_date DATE,
    description VARCHAR(2000),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    selected BOOLEAN NOT NULL DEFAULT FALSE,
    procurement_item_id BIGINT NOT NULL REFERENCES procurement_items(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(100),
    modified_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_procurement_quotes_item ON procurement_quotes(procurement_item_id);
CREATE INDEX IF NOT EXISTS idx_procurement_quotes_item_active ON procurement_quotes (procurement_item_id, active);
CREATE INDEX IF NOT EXISTS idx_procurement_quotes_item_selected ON procurement_quotes (procurement_item_id, selected, active);

-- ============================================================================
-- 20. PROCUREMENT QUOTE FILES
-- ============================================================================
CREATE TABLE IF NOT EXISTS procurement_quote_files (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    data BYTEA NOT NULL,
    description VARCHAR(500),
    quote_id BIGINT NOT NULL REFERENCES procurement_quotes(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_procurement_quote_files_quote ON procurement_quote_files(quote_id);
CREATE INDEX IF NOT EXISTS idx_procurement_quote_files_quote_active ON procurement_quote_files (quote_id, active);

-- ============================================================================
-- 21. AUDIT EVENTS (independent, no FKs, append-only)
-- ============================================================================
-- Note: created_at uses Instant in Java → TIMESTAMP WITH TIME ZONE in Hibernate 6
CREATE TABLE IF NOT EXISTS audit_events (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    entity_name VARCHAR(500),
    rc_id BIGINT,
    rc_name VARCHAR(255),
    fiscal_year_id BIGINT,
    fiscal_year_name VARCHAR(255),
    parameters TEXT,
    http_method VARCHAR(10),
    endpoint VARCHAR(500),
    user_agent VARCHAR(1000),
    ip_address VARCHAR(45),
    outcome VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    cloned_from_audit_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_events_username ON audit_events(username);
CREATE INDEX IF NOT EXISTS idx_audit_events_rc_id ON audit_events(rc_id);
CREATE INDEX IF NOT EXISTS idx_audit_events_rc_fy ON audit_events(rc_id, fiscal_year_id);
CREATE INDEX IF NOT EXISTS idx_audit_events_action ON audit_events(action);
CREATE INDEX IF NOT EXISTS idx_audit_events_entity_type ON audit_events(entity_type);
CREATE INDEX IF NOT EXISTS idx_audit_events_created_at ON audit_events(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_events_outcome ON audit_events(outcome);
CREATE INDEX IF NOT EXISTS idx_audit_events_rc_created ON audit_events(rc_id, created_at DESC);
