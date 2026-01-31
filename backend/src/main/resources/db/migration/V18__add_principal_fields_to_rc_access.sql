-- Cinema Box Office - Add principal fields to rc_access table
-- Copyright (c) 2026 Box Office Team
-- Licensed under MIT License

-- Add principal_type column for distinguishing between users, groups, and distribution lists
ALTER TABLE rc_access ADD COLUMN principal_type VARCHAR(50) NOT NULL DEFAULT 'USER';

-- Add principal_identifier column for storing group/distribution list identifiers
ALTER TABLE rc_access ADD COLUMN principal_identifier VARCHAR(255);

-- Add principal_display_name column for human-readable names
ALTER TABLE rc_access ADD COLUMN principal_display_name VARCHAR(255);

-- Add granted_by_id column for tracking who granted the access
ALTER TABLE rc_access ADD COLUMN granted_by_id INTEGER REFERENCES users(id) ON DELETE SET NULL;

-- Make user_id nullable since groups/distribution lists won't have a user_id
ALTER TABLE rc_access ALTER COLUMN user_id DROP NOT NULL;

-- Update existing records to set principal_identifier and principal_display_name from user data
UPDATE rc_access SET 
    principal_identifier = (SELECT username FROM users WHERE users.id = rc_access.user_id),
    principal_display_name = (SELECT COALESCE(full_name, username) FROM users WHERE users.id = rc_access.user_id)
WHERE user_id IS NOT NULL;

-- Create index for faster lookups by principal_identifier
CREATE INDEX idx_rc_access_principal_identifier ON rc_access(principal_identifier);

-- Add unique constraint for principal-based access (for groups and distribution lists)
-- Note: This is a partial unique index to allow multiple group accesses when combined with user accesses
CREATE UNIQUE INDEX idx_rc_access_unique_principal 
ON rc_access(responsibility_centre_id, principal_identifier, principal_type) 
WHERE user_id IS NULL;
