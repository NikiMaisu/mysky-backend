CREATE TABLE order_materials (
    id             BIGSERIAL PRIMARY KEY,
    order_id       BIGINT NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    material_id    BIGINT REFERENCES materials (id),
    name           VARCHAR(120)   NOT NULL,
    unit_price_m2  NUMERIC(12, 2) NOT NULL,
    unit_time_min  NUMERIC(10, 2) NOT NULL,
    square_meters  NUMERIC(10, 2) NOT NULL,
    CONSTRAINT order_materials_sqm_nonneg CHECK (square_meters >= 0)
);
CREATE INDEX order_materials_order_idx ON order_materials (order_id);

-- Backfill: every existing order's single material/area becomes its first line item.
INSERT INTO order_materials (order_id, material_id, name, unit_price_m2, unit_time_min, square_meters)
SELECT id, material_id, material_name, material_price_per_m2, material_time_per_m2_min, square_meters
FROM orders
WHERE material_name IS NOT NULL;

ALTER TABLE orders
    DROP COLUMN material_id,
    DROP COLUMN material_name,
    DROP COLUMN material_price_per_m2,
    DROP COLUMN material_time_per_m2_min,
    DROP COLUMN square_meters;
