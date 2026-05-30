-- Phase 3: configurable domain objects (materials, fixtures, add-ons, granite, teams)

ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE materials (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(120)   NOT NULL,
    price_per_m2    NUMERIC(12, 2) NOT NULL,
    time_per_m2_min NUMERIC(10, 2) NOT NULL,
    active          BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT materials_price_nonneg CHECK (price_per_m2 >= 0),
    CONSTRAINT materials_time_nonneg  CHECK (time_per_m2_min >= 0)
);

CREATE TABLE fixtures (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(120)   NOT NULL,
    unit             VARCHAR(16)    NOT NULL,
    cost             NUMERIC(12, 2) NOT NULL,
    install_time_min NUMERIC(10, 2) NOT NULL,
    active           BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT fixtures_unit_check   CHECK (unit IN ('PER_UNIT', 'PER_METER')),
    CONSTRAINT fixtures_cost_nonneg  CHECK (cost >= 0),
    CONSTRAINT fixtures_time_nonneg  CHECK (install_time_min >= 0)
);

CREATE TABLE addon_instances (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(120)   NOT NULL,
    category         VARCHAR(24)    NOT NULL DEFAULT 'OTHER',
    cost             NUMERIC(12, 2) NOT NULL,
    install_time_min INTEGER        NOT NULL,
    active           BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT addons_category_check CHECK (category IN ('BLINDS_RAILING', 'HVAC_CUTOUT', 'OTHER')),
    CONSTRAINT addons_cost_nonneg    CHECK (cost >= 0),
    CONSTRAINT addons_time_nonneg    CHECK (install_time_min >= 0)
);

-- Singleton global granite rates (price/time per linear meter). Always row id = 1.
CREATE TABLE granite_config (
    id                  SMALLINT PRIMARY KEY DEFAULT 1,
    price_per_meter     NUMERIC(12, 2) NOT NULL,
    time_per_meter_min  NUMERIC(10, 2) NOT NULL,
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT granite_singleton    CHECK (id = 1),
    CONSTRAINT granite_price_nonneg CHECK (price_per_meter >= 0),
    CONSTRAINT granite_time_nonneg  CHECK (time_per_meter_min >= 0)
);

INSERT INTO granite_config (id, price_per_meter, time_per_meter_min) VALUES (1, 0, 0);

CREATE TABLE teams (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(120) NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE team_members (
    team_id   BIGINT NOT NULL REFERENCES teams (id) ON DELETE CASCADE,
    worker_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (team_id, worker_id)
);

CREATE INDEX team_members_worker_idx ON team_members (worker_id);
