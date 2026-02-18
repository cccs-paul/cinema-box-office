-- myRC - V4 Migration: Training Participants & Travel Travellers
-- Copyright (c) 2026 myRC Team
-- Licensed under MIT License
--
-- Description:
-- Redesigns Training and Travel modules:
--   - Training: new types (COURSE_TRAINING, CONFERENCE_REGISTRATION, OTHER),
--     format field (IN_PERSON, ONLINE), ECO replaces reference_number,
--     1..n participants with individual costs/currency/exchange rate
--   - Travel: new types (DOMESTIC, NORTH_AMERICA, INTERNATIONAL, LOCAL),
--     EMAP replaces reference_number, 1..n travellers with TAAC and approval status

-- ============================================================================
-- 1. TRAINING ITEMS CHANGES
-- ============================================================================

-- 1a. Add format column
ALTER TABLE training_items ADD COLUMN IF NOT EXISTS format VARCHAR(20) NOT NULL DEFAULT 'IN_PERSON';

-- 1b. Rename reference_number to eco
ALTER TABLE training_items RENAME COLUMN reference_number TO eco;

-- 1c. Widen training_type to accommodate longer values like CONFERENCE_REGISTRATION
ALTER TABLE training_items ALTER COLUMN training_type TYPE VARCHAR(30);

-- 1d. Update training types to new values
UPDATE training_items SET training_type = 'COURSE_TRAINING'
    WHERE training_type IN ('COURSE', 'CERTIFICATION', 'WORKSHOP', 'SEMINAR', 'ONLINE');
UPDATE training_items SET training_type = 'CONFERENCE_REGISTRATION'
    WHERE training_type = 'CONFERENCE';
-- 'OTHER' stays as 'OTHER'

-- ============================================================================
-- 2. TRAINING PARTICIPANTS TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS training_participants (
    id BIGSERIAL PRIMARY KEY,
    training_item_id BIGINT NOT NULL REFERENCES training_items(id) ON DELETE CASCADE,
    name VARCHAR(500) NOT NULL,
    estimated_cost NUMERIC(15,2),
    final_cost NUMERIC(15,2),
    currency VARCHAR(3) NOT NULL DEFAULT 'CAD',
    exchange_rate NUMERIC(15,6),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_training_participants_item ON training_participants(training_item_id);

-- 2a. Migrate existing data: create participant from each training item's employee data
INSERT INTO training_participants (training_item_id, name, estimated_cost, final_cost, currency, exchange_rate)
SELECT id,
       COALESCE(employee_name, 'Participant 1'),
       estimated_cost,
       actual_cost,
       currency,
       exchange_rate
FROM training_items
WHERE employee_name IS NOT NULL
   OR estimated_cost IS NOT NULL
   OR actual_cost IS NOT NULL;

-- 2b. Drop migrated columns from training_items
ALTER TABLE training_items DROP COLUMN IF EXISTS employee_name;
ALTER TABLE training_items DROP COLUMN IF EXISTS number_of_participants;
ALTER TABLE training_items DROP COLUMN IF EXISTS estimated_cost;
ALTER TABLE training_items DROP COLUMN IF EXISTS actual_cost;
ALTER TABLE training_items DROP COLUMN IF EXISTS currency;
ALTER TABLE training_items DROP COLUMN IF EXISTS exchange_rate;

-- ============================================================================
-- 3. TRAVEL ITEMS CHANGES
-- ============================================================================

-- 3a. Rename reference_number to emap
ALTER TABLE travel_items RENAME COLUMN reference_number TO emap;

-- 3b. Update travel types to new values
UPDATE travel_items SET travel_type = 'NORTH_AMERICA'
    WHERE travel_type IN ('CONFERENCE', 'TRAINING', 'OTHER');
-- DOMESTIC, INTERNATIONAL, LOCAL stay as-is

-- ============================================================================
-- 4. TRAVEL TRAVELLERS TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS travel_travellers (
    id BIGSERIAL PRIMARY KEY,
    travel_item_id BIGINT NOT NULL REFERENCES travel_items(id) ON DELETE CASCADE,
    name VARCHAR(500) NOT NULL,
    taac VARCHAR(100),
    estimated_cost NUMERIC(15,2),
    final_cost NUMERIC(15,2),
    currency VARCHAR(3) NOT NULL DEFAULT 'CAD',
    exchange_rate NUMERIC(15,6),
    approval_status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_travel_travellers_item ON travel_travellers(travel_item_id);
CREATE INDEX IF NOT EXISTS idx_travel_travellers_status ON travel_travellers(approval_status);

-- 4a. Migrate existing data: create traveller from each travel item's traveller data
INSERT INTO travel_travellers (travel_item_id, name, estimated_cost, final_cost, currency, exchange_rate)
SELECT id,
       COALESCE(traveller_name, 'Traveller 1'),
       estimated_cost,
       actual_cost,
       currency,
       exchange_rate
FROM travel_items
WHERE traveller_name IS NOT NULL
   OR estimated_cost IS NOT NULL
   OR actual_cost IS NOT NULL;

-- 4b. Drop migrated columns from travel_items
ALTER TABLE travel_items DROP COLUMN IF EXISTS travel_authorization_number;
ALTER TABLE travel_items DROP COLUMN IF EXISTS traveller_name;
ALTER TABLE travel_items DROP COLUMN IF EXISTS number_of_travellers;
ALTER TABLE travel_items DROP COLUMN IF EXISTS estimated_cost;
ALTER TABLE travel_items DROP COLUMN IF EXISTS actual_cost;
ALTER TABLE travel_items DROP COLUMN IF EXISTS currency;
ALTER TABLE travel_items DROP COLUMN IF EXISTS exchange_rate;
