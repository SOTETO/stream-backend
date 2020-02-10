# --- !Ups
ALTER TABLE `Source` ADD COLUMN `type_location` VARCHAR(36) AFTER `type_of_source`;
ALTER TABLE `Source` ADD COLUMN `type_contact_person` VARCHAR(36) AFTER `type_location`;
ALTER TABLE `Source` ADD COLUMN `type_email` VARCHAR(36) AFTER `type_contact_person`;
ALTER TABLE `Source` ADD COLUMN `type_address` VARCHAR(36) AFTER `type_email`;
ALTER TABLE `Source` ADD COLUMN `receipt` BOOLEAN AFTER `type_address`;

# --- !Downs
ALTER TABLE `Source` DROP COLUMN `type_location`;
ALTER TABLE `Source` DROP COLUMN `type_contact_person`;
ALTER TABLE `Source` DROP COLUMN `type_email`;
ALTER TABLE `Source` DROP COLUMN `type_address`;
ALTER TABLE `Source` DROP COLUMN `receipt`;

