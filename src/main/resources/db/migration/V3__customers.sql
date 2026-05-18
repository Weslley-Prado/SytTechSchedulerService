-- Customer accounts (the 'NAO' branch of 'Cliente tem cadastro?').
CREATE TABLE customers (
    id                   UUID         PRIMARY KEY,
    full_name            VARCHAR(160) NOT NULL,
    email                VARCHAR(160) NOT NULL UNIQUE,
    phone                VARCHAR(40),
    password_hash        VARCHAR(255) NOT NULL,
    email_verified       BOOLEAN      NOT NULL DEFAULT FALSE,
    email_verify_token   VARCHAR(120),
    email_verify_expires TIMESTAMPTZ,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_customers_email_token ON customers (email_verify_token)
    WHERE email_verify_token IS NOT NULL;
