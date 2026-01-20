-- Cinema Box Office - Add Responsibility Centres Tables
-- Copyright (c) 2026 Box Office Team
-- Licensed under MIT License

-- Create responsibility_centres table
CREATE TABLE responsibility_centres (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    owner_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_owner_rc_name UNIQUE(owner_id, name)
);

-- Create index for faster queries
CREATE INDEX idx_responsibility_centres_owner_id ON responsibility_centres(owner_id);

-- Create rc_access table for managing access control
CREATE TABLE rc_access (
    id SERIAL PRIMARY KEY,
    responsibility_centre_id INTEGER NOT NULL REFERENCES responsibility_centres(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_level VARCHAR(20) NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_rc_user_access UNIQUE(responsibility_centre_id, user_id)
);

-- Create indices for faster queries
CREATE INDEX idx_rc_access_responsibility_centre_id ON rc_access(responsibility_centre_id);
CREATE INDEX idx_rc_access_user_id ON rc_access(user_id);
