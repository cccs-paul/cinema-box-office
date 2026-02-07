-- V31: Add translation_key column to categories table
-- Translation key is used for internationalization (i18n) of default category names.
-- Default categories get translation keys like 'category.compute', 'category.gpus', etc.
-- Custom categories have NULL translation_key and use their user-entered name directly.
ALTER TABLE categories ADD COLUMN translation_key VARCHAR(100);

-- Populate translation keys for existing default categories
UPDATE categories SET translation_key = 'category.compute' WHERE name = 'Compute' AND is_default = true;
UPDATE categories SET translation_key = 'category.gpus' WHERE name = 'GPUs' AND is_default = true;
UPDATE categories SET translation_key = 'category.storage' WHERE name = 'Storage' AND is_default = true;
UPDATE categories SET translation_key = 'category.softwareLicenses' WHERE name = 'Software Licenses' AND is_default = true;
UPDATE categories SET translation_key = 'category.hardwareSupportLicensing' WHERE name = 'Hardware Support/Licensing' AND is_default = true;
UPDATE categories SET translation_key = 'category.smallProcurement' WHERE name = 'Small Procurement' AND is_default = true;
UPDATE categories SET translation_key = 'category.contractors' WHERE name = 'Contractors' AND is_default = true;
