-- Schema migration for existing orders to support coupon discount amount

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS discount_amount numeric(38,2);

UPDATE orders
    SET discount_amount = 0
    WHERE discount_amount IS NULL;

ALTER TABLE orders
    ALTER COLUMN discount_amount SET NOT NULL;
