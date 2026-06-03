-- Configurable work schedule (company-wide singleton + optional per-team override)
-- work_days is a bitmask: Mon=1, Tue=2, Wed=4, Thu=8, Fri=16, Sat=32, Sun=64.

CREATE TABLE work_schedule (
    id         SMALLINT PRIMARY KEY DEFAULT 1,
    work_days  SMALLINT    NOT NULL,
    start_time TIME        NOT NULL,
    end_time   TIME        NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT work_schedule_singleton CHECK (id = 1),
    CONSTRAINT work_schedule_days      CHECK (work_days > 0),
    CONSTRAINT work_schedule_time      CHECK (end_time > start_time)
);

-- Default: Mon–Sat (1+2+4+8+16+32 = 63), 10:00–18:00.
INSERT INTO work_schedule (id, work_days, start_time, end_time) VALUES (1, 63, '10:00', '18:00');

-- Per-team override (all three null = inherit global).
ALTER TABLE teams
    ADD COLUMN work_days  SMALLINT,
    ADD COLUMN work_start TIME,
    ADD COLUMN work_end   TIME,
    ADD CONSTRAINT teams_work_time CHECK (work_end IS NULL OR work_start IS NULL OR work_end > work_start);

-- Orders: track when the finish time is a manual override vs the computed recommendation.
ALTER TABLE orders ADD COLUMN finish_overridden BOOLEAN NOT NULL DEFAULT FALSE;
