# --- !Ups
ALTER TABLE `Deposit` ADD COLUMN `confirmed_user_uuid` VARCHAR(36) AFTER `confirmed`;
ALTER TABLE `Deposit` ADD COLUMN `confirmed_user_name` VARCHAR(36) AFTER `confirmed_user_uuid`;
ALTER TABLE `Deposit` ADD COLUMN `supporter_name` VARCHAR(36) NOT NULL AFTER `supporter`;
ALTER TABLE `Deposit_Unit` ADD COLUMN `confirmed_user_uuid` VARCHAR(36) AFTER `confirmed`;
ALTER TABLE `Deposit_Unit` ADD COLUMN `confirmed_user_name` VARCHAR(36) AFTER `confirmed_user_uuid`;

# --- !Downs

ALTER TABLE `Deposit` DROP COLUMN `supporter_name`;
ALTER TABLE `Deposit` DROP COLUMN `confirmed_user_uuid`;
ALTER TABLE `Deposit` DROP COLUMN `confirmed_user_name`;
ALTER TABLE `Deposit_Unit` DROP COLUMN `confirmed_user_uuid`;
ALTER TABLE `Deposit_Unit` DROP COLUMN `confirmed_user_name`;



