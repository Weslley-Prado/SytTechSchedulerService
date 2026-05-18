-- Units (saloes/filiais) and their weekly business hours.
CREATE TABLE units (
    id              UUID         PRIMARY KEY,
    name            VARCHAR(160) NOT NULL,
    address         VARCHAR(255),
    city            VARCHAR(120),
    phone           VARCHAR(40),
    email           VARCHAR(160),
    cover_image_url VARCHAR(500),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_units_city ON units (city) WHERE active = TRUE;

CREATE TABLE business_hours (
    id          UUID         PRIMARY KEY,
    unit_id     UUID         NOT NULL REFERENCES units(id) ON DELETE CASCADE,
    day_of_week SMALLINT     NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    opens_at    TIME         NOT NULL,
    closes_at   TIME         NOT NULL,
    CONSTRAINT business_hours_window_valid CHECK (closes_at > opens_at),
    CONSTRAINT business_hours_unique UNIQUE (unit_id, day_of_week)
);
