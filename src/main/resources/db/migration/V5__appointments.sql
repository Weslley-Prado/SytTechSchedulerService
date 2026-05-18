-- Confirmed appointments produced from a Hold.
CREATE TABLE appointments (
    id              UUID         PRIMARY KEY,
    unit_id         UUID         NOT NULL REFERENCES units(id),
    service_id      UUID         NOT NULL REFERENCES services(id),
    professional_id UUID         NOT NULL REFERENCES professionals(id),
    customer_id     UUID         NOT NULL REFERENCES customers(id),
    code            VARCHAR(16)  NOT NULL UNIQUE,
    status          VARCHAR(16)  NOT NULL,
    start_at        TIMESTAMPTZ  NOT NULL,
    end_at          TIMESTAMPTZ  NOT NULL,
    cancelled_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT appointments_status_valid
        CHECK (status IN ('CONFIRMED','CANCELLED','COMPLETED','NO_SHOW')),
    CONSTRAINT appointments_window_valid CHECK (end_at > start_at)
);
-- A professional cannot have two active appointments at the same start instant.
CREATE UNIQUE INDEX idx_appointments_unique_active_slot
    ON appointments (professional_id, start_at)
    WHERE status = 'CONFIRMED';
CREATE INDEX idx_appointments_customer ON appointments (customer_id);
CREATE INDEX idx_appointments_window   ON appointments (start_at, end_at);
