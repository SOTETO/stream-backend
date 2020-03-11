# --- !Ups
RENAME TABLE Donation TO Taking;
ALTER TABLE Deposit_Unit CHANGE donation_id taking_id BIGINT(20);
ALTER TABLE Source CHANGE donation_id taking_id BIGINT(20);
ALTER TABLE InvolvedSupporter CHANGE donation_id taking_id BIGINT(20);

# --- !Downs
RENAME TABLE Taking TO Donation;
ALTER TABLE Deposit_Unit CHANGE taking_id donation_id BIGINT(20);
ALTER TABLE Source CHANGE donation_id taking_id BIGINT(20);
ALTER TABLE InvolvedSupporter CHANGE donation_id taking_id BIGINT(20);


