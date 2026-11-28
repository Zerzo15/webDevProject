-- V3__otp_hardening_and_orders_alignment.sql

-- OTP: room for a hashed code, and a counter to limit wrong guesses
ALTER TABLE otp_codes
    MODIFY COLUMN code VARCHAR(64) NOT NULL;

ALTER TABLE otp_codes
    ADD COLUMN attempts INT NOT NULL DEFAULT 0;

-- OTP / refresh tokens: the D1-8 cleanup job deletes by expiry, so index it
CREATE INDEX idx_otp_codes_expires_at ON otp_codes (expires_at);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);

-- Orders: align the DB with the Order entity (TEXT NOT NULL / NOT NULL)
-- Backfill first so the NOT NULL change cannot fail on existing rows
UPDATE orders SET shipping_address_text = '' WHERE shipping_address_text IS NULL;
UPDATE orders SET total_amount = 0 WHERE total_amount IS NULL;

ALTER TABLE orders
    MODIFY COLUMN shipping_address_text TEXT NOT NULL,
    MODIFY COLUMN total_amount DECIMAL(12,2) NOT NULL;