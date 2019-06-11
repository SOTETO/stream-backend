# DepositUnit Extension

# --- !Ups

ALTER TABLE Deposit_Unit ADD COLUMN `donation_id` BIGINT(20) NOT NULL;
ALTER TABLE Deposit_Unit ADD CONSTRAINT FK_DONATION FOREIGN KEY (donation_id) REFERENCES Donation(id) on DELETE CASCADE;

# --- !Downs

ALTER TABLE Deposit_Unit DELETE CONSTRAINT FK_DONATION;
ALTER TABLE Deposit_Unit DELETE COLUMN `donation_id`;