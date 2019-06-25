# Use date to indicate confirmation

# --- !Ups

ALTER TABLE Deposit_Unit DROP COLUMN `received`;
ALTER TABLE Deposit_Unit ADD COLUMN `confirmed` BIGINT(20) DEFAULT NULL AFTER `public_id`;

ALTER TABLE Deposit DROP COLUMN `state`;
ALTER TABLE Deposit ADD COLUMN `confirmed` BIGINT(20) DEFAULT NULL AFTER `public_id`;

# --- !Downs

ALTER TABLE Deposit_Unit DROP COLUMN `confirmed`;
ALTER TABLE Deposit_Unit ADD COLUMN `received` BIGINT(20) DEFAULT NULL AFTER `public_id`;

ALTER TABLE Deposit DROP COLUMN `confirmed`;
ALTER TABLE Deposit ADD COLUMN `state` VARCHAR(36) NOT NULL AFTER `public_id`;