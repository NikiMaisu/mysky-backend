-- mysky initial schema placeholder.
-- Real tables are introduced in phase 2 (users) and phase 3 (domain objects).
-- This file exists so Flyway has at least one migration to apply on first run.

CREATE TABLE IF NOT EXISTS schema_bootstrap (
    id          SMALLINT PRIMARY KEY,
    initialized BOOLEAN  NOT NULL DEFAULT TRUE
);

INSERT INTO schema_bootstrap (id) VALUES (1) ON CONFLICT DO NOTHING;
