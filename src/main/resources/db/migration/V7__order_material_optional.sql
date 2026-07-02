ALTER TABLE orders
    ALTER COLUMN material_name DROP NOT NULL,
    ALTER COLUMN material_price_per_m2 DROP NOT NULL,
    ALTER COLUMN material_time_per_m2_min DROP NOT NULL,
    ALTER COLUMN square_meters DROP NOT NULL;
