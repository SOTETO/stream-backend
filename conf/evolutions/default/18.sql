# --- !Ups
ALTER TABLE `Deposit` ADD COLUMN `crew_name` VARCHAR(36) NOT NULL AFTER `crew`;

# --- !Downs
ALTER TABLE `Deposit` DROP COLUMN `crew_name`;
