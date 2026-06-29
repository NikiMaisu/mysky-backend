ALTER TABLE users
    ALTER COLUMN email DROP NOT NULL,
    ADD COLUMN phone VARCHAR(32);

ALTER TABLE users
    ADD CONSTRAINT users_phone_unique UNIQUE (phone),
    ADD CONSTRAINT users_contact_check CHECK (email IS NOT NULL OR phone IS NOT NULL);
