CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email             VARCHAR(255) NOT NULL UNIQUE,
    password_hash     VARCHAR(255) NOT NULL,
    first_name        VARCHAR(100) NOT NULL,
    last_name         VARCHAR(100) NOT NULL,
    role              VARCHAR(20)  NOT NULL DEFAULT 'USER',
    active            BOOLEAN      NOT NULL DEFAULT TRUE,
    email_verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by        VARCHAR(255),
    updated_by        VARCHAR(255),
    version           BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role  ON users(role);
