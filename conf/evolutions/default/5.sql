# Fix missing date_of_deposit column

# --- !Ups

ALTER TABLE Deposit ADD COLUMN `date_of_deposit` BIGINT(20) NOT NULL;

# --- !Downs

ALTER TABLE Deposit DROP COLUMN `date_of_deposit`;