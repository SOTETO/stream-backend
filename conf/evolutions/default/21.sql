# --- !Ups
ALTER TABLE `Source` ADD COLUMN `description` VARCHAR(255) AFTER `category`;

# --- !Downs
ALTER TABLE `Source` DROP COLUMN `description`;
