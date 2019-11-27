
# --- !Ups

ALTER TABLE `Source` ADD COLUMN `public_id` VARCHAR(36) NOT NULL AFTER `id`;

# --- !Downs

ALTER TABLE `Source` DROP COLUMN `public_id`;
