SET search_path TO app_schema_users_service;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS status     VARCHAR(20)  NOT NULL DEFAULT 'Pending',
    ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS last_login TIMESTAMP;