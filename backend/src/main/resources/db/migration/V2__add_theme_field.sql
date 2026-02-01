-- Liquibase migration: Add theme field to users table
-- Author: myRC Team
-- Date: 2026-01-17
-- Version: 2

-- changeset myrc:add-theme-field
-- comment: Add theme column to store user theme preference (light/dark)

ALTER TABLE users ADD COLUMN theme VARCHAR(20) DEFAULT 'light' NOT NULL;
