-- Temporary pre-reservation. Released either explicitly or by TTL job.
CREATE TABLE holds (
    id              UUID        PRIMARY KEY,
    unit_id         UUID        NOT NULL REFERENCES units(id)         ON DELETE CASCADE,
    service_id      UUID        NOT NULL REFERENCES services(id)      ON DELETE CASCADE,
    professional_id UUID        NOT NULL REFERENCES professionals(id) ON DELETE CASCADE,
    start_at        TIMESTAMPTZ NOT NULL,
    end_at          TIMESTAMPTZ NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    consumed        BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT holds_window_valid CHECK (end_at > start_at)
);
-- A given professional cannot have two active (not consumed, not expired) holds
-- for the same start instant.
CREATE UNIQUE INDEX idx_holds_unique_active_slot
    ON holds (professional_id, start_at)
    WHERE consumed = FALSE;
CREATE INDEX idx_holds_expires ON holds (expires_at) WHERE consumed = FALSE;
