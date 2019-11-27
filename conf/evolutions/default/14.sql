
# --- !Ups
ALTER TABLE `Taking` DROP COLUMN `norms`;
ALTER TABLE `Source` ADD COLUMN `norms` VARCHAR(36) NOT NULL;

# --- !Downs
ALTER TABLE `Taking` ADD COLUMN `norms` VARCHAR(36) NOT NULL AFTER `category`;
ALTER TABLE `Source` DROP COLUMN `norms`;
