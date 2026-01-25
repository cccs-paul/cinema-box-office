-- myRC - Add Monies Table
-- Copyright (c) 2026 myRC Team
-- Licensed under MIT License
--
-- Author: myRC Team
-- Date: 2026-01-24
-- Version: 1.0.0
--
-- Description:
-- Creates the monies table for storing money types within fiscal years.
-- Each money type has two parts: Capital (CAP) and O&M (OM).
-- Every fiscal year has a default "AB" money, plus additional custom monies.

-- Create monies table
CREATE TABLE monies (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    fiscal_year_id INTEGER NOT NULL REFERENCES fiscal_years(id) ON DELETE CASCADE,
    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_money_code_fy UNIQUE(code, fiscal_year_id)
);

-- Create indexes for faster queries
CREATE INDEX idx_monies_fiscal_year_id ON monies(fiscal_year_id);
CREATE INDEX idx_monies_code ON monies(code);
CREATE INDEX idx_monies_is_default ON monies(is_default);
CREATE INDEX idx_monies_active ON monies(active);
CREATE INDEX idx_monies_display_order ON monies(display_order);

-- Add comments for documentation
COMMENT ON TABLE monies IS 'Stores money types associated with fiscal years. Each money has CAP and OM parts.';
COMMENT ON COLUMN monies.code IS 'Short code for the money type (e.g., AB, OA, WCF)';
COMMENT ON COLUMN monies.name IS 'Descriptive name for the money type (e.g., A-Base, Operating Allotment)';
COMMENT ON COLUMN monies.description IS 'Optional detailed description of the money type';
COMMENT ON COLUMN monies.is_default IS 'Whether this is the system default money (AB). Cannot be deleted.';
COMMENT ON COLUMN monies.display_order IS 'Order in which monies are displayed in the UI';
COMMENT ON COLUMN monies.active IS 'Whether the money type is currently active';

-- Create function to ensure default AB money exists for each fiscal year
CREATE OR REPLACE FUNCTION ensure_default_money()
RETURNS TRIGGER AS $$
BEGIN
    -- Insert default AB money for new fiscal year
    INSERT INTO monies (code, name, description, is_default, fiscal_year_id, display_order)
    VALUES ('AB', 'A-Base', 'Default A-Base funding allocation', TRUE, NEW.id, 0);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically create default money when a fiscal year is created
CREATE TRIGGER trg_create_default_money
    AFTER INSERT ON fiscal_years
    FOR EACH ROW
    EXECUTE FUNCTION ensure_default_money();

-- Create default AB money for all existing fiscal years
INSERT INTO monies (code, name, description, is_default, fiscal_year_id, display_order)
SELECT 'AB', 'A-Base', 'Default A-Base funding allocation', TRUE, fy.id, 0
FROM fiscal_years fy
WHERE NOT EXISTS (
    SELECT 1 FROM monies m WHERE m.fiscal_year_id = fy.id AND m.code = 'AB'
);
