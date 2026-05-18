-- Categories, services and professionals offered by each unit.
CREATE TABLE categories (
    id       UUID         PRIMARY KEY,
    unit_id  UUID         NOT NULL REFERENCES units(id) ON DELETE CASCADE,
    name     VARCHAR(120) NOT NULL,
    icon_url VARCHAR(500),
    CONSTRAINT categories_unit_name_unique UNIQUE (unit_id, name)
);

CREATE TABLE services (
    id               UUID           PRIMARY KEY,
    category_id      UUID           NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name             VARCHAR(160)   NOT NULL,
    description      TEXT,
    duration_minutes INTEGER        NOT NULL CHECK (duration_minutes > 0),
    price            NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    currency         CHAR(3)        NOT NULL DEFAULT 'BRL',
    active           BOOLEAN        NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_services_category ON services (category_id);

CREATE TABLE professionals (
    id         UUID         PRIMARY KEY,
    unit_id    UUID         NOT NULL REFERENCES units(id) ON DELETE CASCADE,
    name       VARCHAR(160) NOT NULL,
    avatar_url VARCHAR(500),
    rating     NUMERIC(2, 1) CHECK (rating BETWEEN 0 AND 5),
    active     BOOLEAN      NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_professionals_unit ON professionals (unit_id) WHERE active = TRUE;

CREATE TABLE service_professionals (
    service_id      UUID NOT NULL REFERENCES services(id)      ON DELETE CASCADE,
    professional_id UUID NOT NULL REFERENCES professionals(id) ON DELETE CASCADE,
    PRIMARY KEY (service_id, professional_id)
);
