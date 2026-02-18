-- V5: Split currency/exchangeRate into estimated/final for participants and travellers,
--     move ECO from training_items to training_participants,
--     add participant status, and add include-in-summary flags to responsibility_centres.

-- ============================================================
-- 1. Responsibility Centres: include-in-summary toggles
-- ============================================================
ALTER TABLE responsibility_centres
    ADD COLUMN training_include_in_summary BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE responsibility_centres
    ADD COLUMN travel_include_in_summary BOOLEAN NOT NULL DEFAULT FALSE;

-- ============================================================
-- 2. Training Participants: eco, status, split currencies
-- ============================================================
-- Add ECO (moved from training_items)
ALTER TABLE training_participants
    ADD COLUMN eco VARCHAR(100);

-- Add participant status
ALTER TABLE training_participants
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'PLANNED';

-- Rename existing currency -> estimated_currency
ALTER TABLE training_participants
    RENAME COLUMN currency TO estimated_currency;

-- Rename existing exchange_rate -> estimated_exchange_rate
ALTER TABLE training_participants
    RENAME COLUMN exchange_rate TO estimated_exchange_rate;

-- Add final currency columns
ALTER TABLE training_participants
    ADD COLUMN final_currency VARCHAR(3) NOT NULL DEFAULT 'CAD';

ALTER TABLE training_participants
    ADD COLUMN final_exchange_rate DECIMAL(15, 6);

-- ============================================================
-- 3. Travel Travellers: split currencies
-- ============================================================
-- Rename existing currency -> estimated_currency
ALTER TABLE travel_travellers
    RENAME COLUMN currency TO estimated_currency;

-- Rename existing exchange_rate -> estimated_exchange_rate
ALTER TABLE travel_travellers
    RENAME COLUMN exchange_rate TO estimated_exchange_rate;

-- Add final currency columns
ALTER TABLE travel_travellers
    ADD COLUMN final_currency VARCHAR(3) NOT NULL DEFAULT 'CAD';

ALTER TABLE travel_travellers
    ADD COLUMN final_exchange_rate DECIMAL(15, 6);

-- ============================================================
-- 4. Training Items: drop eco column (moved to participants)
-- ============================================================
ALTER TABLE training_items
    DROP COLUMN IF EXISTS eco;
