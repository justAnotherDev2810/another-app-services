-- V6__create_expenses_table.sql
-- Creates the expenses table with FK references to users and categories.

SET search_path TO app_schema_users_service;

CREATE TABLE IF NOT EXISTS expenses
(
    id           BIGSERIAL      NOT NULL,
    user_id      BIGINT         NOT NULL,
    category_id  BIGINT         NOT NULL,
    amount       NUMERIC(19, 4) NOT NULL,
    description  VARCHAR(500),
    expense_date DATE           NOT NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_expenses          PRIMARY KEY (id),
    CONSTRAINT fk_expense_user      FOREIGN KEY (user_id)
    REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_expense_category  FOREIGN KEY (category_id)
    REFERENCES categories (id) ON DELETE RESTRICT
    );

-- Index on user_id — most queries will filter by user
CREATE INDEX IF NOT EXISTS idx_expenses_user_id ON expenses (user_id);

-- Index on expense_date — supports date range filters efficiently
CREATE INDEX IF NOT EXISTS idx_expenses_expense_date ON expenses (expense_date);

-- Composite index for the most common combined filter (user + date range)
CREATE INDEX IF NOT EXISTS idx_expenses_user_date ON expenses (user_id, expense_date);