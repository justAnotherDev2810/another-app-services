-- V5__seed_categories.sql
-- Seeds default categories.
-- Uses INSERT ... ON CONFLICT DO NOTHING so re-running is safe
-- and adding new categories via UI won't conflict with this seed.

SET search_path TO app_schema_users_service;

INSERT INTO categories (name) VALUES
                                  ('Food'),
                                  ('Travel'),
                                  ('Utilities'),
                                  ('Bills'),
                                  ('Entertainment'),
                                  ('Other')
    ON CONFLICT (name) DO NOTHING;