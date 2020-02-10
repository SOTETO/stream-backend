# Add currency information and an attribute to save the full amount of an deposit (supports unassignable deposits)

# --- !Ups

ALTER TABLE Deposit_Unit ADD COLUMN `currency` VARCHAR(3) DEFAULT 'EUR' AFTER `amount`;
ALTER TABLE Deposit ADD COLUMN `full_amount` DOUBLE(10,2) DEFAULT 0.0 AFTER `public_id`;
ALTER TABLE Deposit ADD COLUMN `currency` VARCHAR(3) DEFAULT 'EUR' AFTER `full_amount`;

# --- !Downs

ALTER TABLE Deposit_Unit DROP COLUMN `currency`;
ALTER TABLE Deposit DROP COLUMN `full_amount`;
ALTER TABLE Deposit DROP COLUMN `currency`;