# --- !Ups
ALTER TABLE `Taking` DROP COLUMN `crew`;
ALTER TABLE `InvolvedSupporter` ADD COLUMN `name` VARCHAR(36) NOT NULL;

# --- !Downs
ALTER TABLE `Taking` ADD COLUMN `crew` VARCHAR(36) NOT NULL AFTER `author`;
ALTER TABLE `InvolvedSupporter` DROP COLUMN `name`;
