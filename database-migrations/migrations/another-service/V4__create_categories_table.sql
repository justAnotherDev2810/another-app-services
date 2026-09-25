-- V4__create_categories_table.sql
-- Creates the categories table for another-service

SET search_path TO app_schema_users_service;

CREATE TABLE IF NOT EXISTS categories
(
    id   BIGSERIAL    NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,

    CONSTRAINT pk_categories PRIMARY KEY (id)
);