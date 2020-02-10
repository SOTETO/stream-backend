#add norms to Donations

# --- !Ups

ALTER TABLE `Donation` ADD COLUMN `norms` VARCHAR(36) NOT NULL AFTER `category`;

# --- !Downs

ALTER TABLE `Donation` DROP COLUMN `norms`;
