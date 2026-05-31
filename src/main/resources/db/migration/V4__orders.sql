-- Phase 4: orders + line items (snapshot pricing/time at save)

CREATE SEQUENCE order_number_seq START WITH 1001 INCREMENT BY 1;

CREATE TABLE orders (
    id                          BIGSERIAL PRIMARY KEY,
    order_number                BIGINT NOT NULL UNIQUE DEFAULT nextval('order_number_seq'),
    client_name                 VARCHAR(160)   NOT NULL,
    client_phone                VARCHAR(40),
    address                     VARCHAR(400),
    start_at                    TIMESTAMPTZ    NOT NULL,
    finish_at                   TIMESTAMPTZ    NOT NULL,

    team_id                     BIGINT REFERENCES teams (id),
    team_name                   VARCHAR(120),

    material_id                 BIGINT REFERENCES materials (id),
    material_name               VARCHAR(120)   NOT NULL,
    material_price_per_m2       NUMERIC(12, 2) NOT NULL,
    material_time_per_m2_min    NUMERIC(10, 2) NOT NULL,
    square_meters               NUMERIC(10, 2) NOT NULL,

    granite_enabled             BOOLEAN        NOT NULL DEFAULT FALSE,
    perimeter                   NUMERIC(10, 2),
    granite_price_per_meter     NUMERIC(12, 2),
    granite_time_per_meter_min  NUMERIC(10, 2),

    flat_added_min              INTEGER        NOT NULL DEFAULT 0,
    total_min                   INTEGER        NOT NULL,
    total_cost                  NUMERIC(12, 2) NOT NULL,

    status                      VARCHAR(16)    NOT NULL,
    notes                       TEXT,

    created_at                  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT orders_status_check CHECK (status IN ('QUOTED','SCHEDULED','IN_PROGRESS','DONE','CANCELLED')),
    CONSTRAINT orders_sqm_nonneg   CHECK (square_meters >= 0),
    CONSTRAINT orders_granite_perimeter CHECK (NOT granite_enabled OR perimeter IS NOT NULL)
);
CREATE INDEX orders_start_idx  ON orders (start_at);
CREATE INDEX orders_team_idx   ON orders (team_id);
CREATE INDEX orders_status_idx ON orders (status);

CREATE TABLE order_fixtures (
    id            BIGSERIAL PRIMARY KEY,
    order_id      BIGINT NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    fixture_id    BIGINT REFERENCES fixtures (id),
    name          VARCHAR(120)   NOT NULL,
    unit          VARCHAR(16)    NOT NULL,
    unit_cost     NUMERIC(12, 2) NOT NULL,
    unit_time_min NUMERIC(10, 2) NOT NULL,
    quantity      NUMERIC(10, 2) NOT NULL,
    CONSTRAINT order_fixtures_qty_nonneg CHECK (quantity >= 0)
);
CREATE INDEX order_fixtures_order_idx ON order_fixtures (order_id);

CREATE TABLE order_addons (
    id            BIGSERIAL PRIMARY KEY,
    order_id      BIGINT NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    addon_id      BIGINT REFERENCES addon_instances (id),
    name          VARCHAR(120)   NOT NULL,
    category      VARCHAR(24)    NOT NULL,
    unit_cost     NUMERIC(12, 2) NOT NULL,
    unit_time_min INTEGER        NOT NULL,
    quantity      INTEGER        NOT NULL,
    CONSTRAINT order_addons_qty_nonneg CHECK (quantity >= 0)
);
CREATE INDEX order_addons_order_idx ON order_addons (order_id);
