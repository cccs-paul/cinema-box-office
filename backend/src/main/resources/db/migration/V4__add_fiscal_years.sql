-- Cinema Box Office - Add Fiscal Years Table
-- Copyright (c) 2026 Box Office Team
-- Licensed under MIT License

-- Create fiscal_years table
CREATE TABLE fiscal_years (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    responsibility_centre_id INTEGER NOT NULL REFERENCES responsibility_centres(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_fiscal_year_name_per_rc UNIQUE(name, responsibility_centre_id),
    CONSTRAINT fiscal_year_dates_valid CHECK (end_date > start_date)
);

-- Create index for faster queries
CREATE INDEX idx_fiscal_years_responsibility_centre_id ON fiscal_years(responsibility_centre_id);
CREATE INDEX idx_fiscal_years_active ON fiscal_years(active);
CREATE INDEX idx_fiscal_years_dates ON fiscal_years(start_date, end_date);

-- Add comment for documentation
COMMENT ON TABLE fiscal_years IS 'Stores fiscal years associated with responsibility centres';
COMMENT ON COLUMN fiscal_years.name IS 'Name of the fiscal year (e.g., FY 2025-2026)';
COMMENT ON COLUMN fiscal_years.start_date IS 'Start date of the fiscal year';
COMMENT ON COLUMN fiscal_years.end_date IS 'End date of the fiscal year';
COMMENT ON COLUMN fiscal_years.active IS 'Whether the fiscal year is currently active';
