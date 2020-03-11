# Add crew id to Household

# --- !Ups

ALTER TABLE `Household_Version` ADD COLUMN `crew_id` VARCHAR(36) NOT NULL AFTER `bic`;

# --- !Downs

ALTER TABLE `Household_Version` DROP COLUMN `crew_id`;