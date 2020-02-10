# Add crew id to donation table

# --- !Ups

ALTER TABLE Donation ADD COLUMN `crew` VARCHAR(36) NOT NULL AFTER `author`;

# -- !Downs

ALTER TABLE Donation DROP COLUMN `crew`;