-- myRC - V2 Migration: Training and Travel Tables
-- Copyright (c) 2026 myRC Team
-- Licensed under MIT License
--
-- Description:
-- Adds training_items, training_money_allocations, travel_items,
-- and travel_money_allocations tables for the Training and Travel modules.
--
-- Table creation order respects foreign key dependencies:
--   1. training_items (→ fiscal_years)
--   2. training_money_allocations (→ training_items, monies)
--   3. travel_items (→ fiscal_years)
--   4. travel_money_allocations (→ travel_items, monies)

-- ============================================================================
-- 1. TRAINING ITEMS
-- ============================================================================
CREATE TABLE IF NOT EXISTS training_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    provider VARCHAR(200),
    reference_number VARCHAR(100),
    estimated_cost NUMERIC(15,2),
    actual_cost NUMERIC(15,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    training_type VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    currency VARCHAR(3) NOT NULL DEFAULT 'CAD',
    exchange_rate NUMERIC(15,6),
    start_date DATE,
    end_date DATE,
    location VARCHAR(500),
    employee_name VARCHAR(500),
    number_of_participants INTEGER,
    fiscal_year_id BIGINT NOT NULL REFERENCES fiscal_years(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_ti_name_fy UNIQUE (name, fiscal_year_id)
);

CREATE INDEX IF NOT EXISTS idx_training_items_fy ON training_items(fiscal_year_id);
CREATE INDEX IF NOT EXISTS idx_training_items_fy_active ON training_items(fiscal_year_id, active);
CREATE INDEX IF NOT EXISTS idx_training_items_name_fy ON training_items(fiscal_year_id, name);
CREATE INDEX IF NOT EXISTS idx_training_items_status ON training_items(status);
CREATE INDEX IF NOT EXISTS idx_training_items_type ON training_items(training_type);

-- ============================================================================
-- 2. TRAINING MONEY ALLOCATIONS (OM only)
-- ============================================================================
CREATE TABLE IF NOT EXISTS training_money_allocations (
    id BIGSERIAL PRIMARY KEY,
    training_item_id BIGINT NOT NULL REFERENCES training_items(id) ON DELETE CASCADE,
    money_id BIGINT NOT NULL REFERENCES monies(id) ON DELETE CASCADE,
    om_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_training_allocation_ti_money UNIQUE (training_item_id, money_id)
);

CREATE INDEX IF NOT EXISTS idx_training_money_alloc_training_item_id ON training_money_allocations(training_item_id);
CREATE INDEX IF NOT EXISTS idx_training_money_alloc_money_id ON training_money_allocations(money_id);
CREATE INDEX IF NOT EXISTS idx_training_money_alloc_ti_money ON training_money_allocations(training_item_id, money_id);

-- ============================================================================
-- 3. TRAVEL ITEMS
-- ============================================================================
CREATE TABLE IF NOT EXISTS travel_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    travel_authorization_number VARCHAR(100),
    reference_number VARCHAR(100),
    destination VARCHAR(500),
    purpose VARCHAR(2000),
    estimated_cost NUMERIC(15,2),
    actual_cost NUMERIC(15,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    travel_type VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    currency VARCHAR(3) NOT NULL DEFAULT 'CAD',
    exchange_rate NUMERIC(15,6),
    departure_date DATE,
    return_date DATE,
    traveller_name VARCHAR(500),
    number_of_travellers INTEGER,
    fiscal_year_id BIGINT NOT NULL REFERENCES fiscal_years(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_trv_name_fy UNIQUE (name, fiscal_year_id)
);

CREATE INDEX IF NOT EXISTS idx_travel_items_fy ON travel_items(fiscal_year_id);
CREATE INDEX IF NOT EXISTS idx_travel_items_fy_active ON travel_items(fiscal_year_id, active);
CREATE INDEX IF NOT EXISTS idx_travel_items_name_fy ON travel_items(fiscal_year_id, name);
CREATE INDEX IF NOT EXISTS idx_travel_items_status ON travel_items(status);
CREATE INDEX IF NOT EXISTS idx_travel_items_type ON travel_items(travel_type);

-- ============================================================================
-- 4. TRAVEL MONEY ALLOCATIONS (OM only)
-- ============================================================================
CREATE TABLE IF NOT EXISTS travel_money_allocations (
    id BIGSERIAL PRIMARY KEY,
    travel_item_id BIGINT NOT NULL REFERENCES travel_items(id) ON DELETE CASCADE,
    money_id BIGINT NOT NULL REFERENCES monies(id) ON DELETE CASCADE,
    om_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_travel_allocation_trv_money UNIQUE (travel_item_id, money_id)
);

CREATE INDEX IF NOT EXISTS idx_travel_money_alloc_travel_item_id ON travel_money_allocations(travel_item_id);
CREATE INDEX IF NOT EXISTS idx_travel_money_alloc_money_id ON travel_money_allocations(money_id);
CREATE INDEX IF NOT EXISTS idx_travel_money_alloc_trv_money ON travel_money_allocations(travel_item_id, money_id);
