# Donation Schema

# --- !Ups
CREATE TABLE Donation (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  public_id VARCHAR(36) NOT NULL,
  received BIGINT(20) NOT NULL,
  description VARCHAR(255) NOT NULL,
  category VARCHAR(255) NOT NULL,
  comment MEDIUMTEXT,
  reason_for_payment VARCHAR(255),
  receipt TINYINT(1),
  author VARCHAR(36) NOT NULL,
  created BIGINT(20) NOT NULL,
  updated BIGINT(20) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT public_id_constraint UNIQUE (public_id)
);

CREATE TABLE InvolvedSupporter (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  donation_id BIGINT(20) NOT NULL,
  supporter_id VARCHAR(36) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_Sup_Donation FOREIGN KEY (donation_id) REFERENCES Donation(id) ON DELETE CASCADE
);

CREATE TABLE Source (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  donation_id BIGINT(20) NOT NULL,
  category VARCHAR(255) NOT NULL,
  amount DOUBLE NOT NULL,
  currency VARCHAR(5) NOT NULL DEFAULT 'EUR',
  type_of_source VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_Sou_Donation FOREIGN KEY (donation_id) REFERENCES Donation(id) ON DELETE CASCADE
);



# --- !Ups
CREATE Table Deposit (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  public_id VARCHAR(36) NOT NULL,
  state VARCHAR(36) NOT NULL,
  crew VARCHAR(36) NOT NULL,
  supporter VARCHAR(36) NOT NULL,
  created BIGINT(20) NOT NULL,
  updated BIGINT(20) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT public_id_constraint UNIQUE (public_id)
);

CREATE Table Deposit_Unit (
  id BIGINT(20) NOT NULL,
  public_id VARCHAR(36) NOT NULL,
  received VARCHAR(36) NOT NULL,
  amount VARCHAR(36) NOT NULL,
  created BIGINT(20) NOT NULL,
  updated BIGINT(20) NOT NULL,
  deposit_id BIGINT(20) NOT NULL, 
  PRIMARY KEY (id),
  CONSTRAINT public_id_constraint UNIQUE (public_id),
  CONSTRAINT FK_DEPOSIT FOREIGN KEY (deposit_id) REFERENCES Deposit(id) on DELETE CASCADE
);

# --- !Ups

ALTER TABLE Deposit_Unit ADD COLUMN `donation_id` BIGINT(20) NOT NULL;
ALTER TABLE Deposit_Unit ADD CONSTRAINT FK_DONATION FOREIGN KEY (donation_id) REFERENCES Donation(id) on DELETE CASCADE;



# --- !Ups

ALTER TABLE Deposit_Unit DROP COLUMN `received`;
ALTER TABLE Deposit_Unit ADD COLUMN `confirmed` BIGINT(20) DEFAULT NULL AFTER `public_id`;

ALTER TABLE Deposit DROP COLUMN `state`;
ALTER TABLE Deposit ADD COLUMN `confirmed` BIGINT(20) DEFAULT NULL AFTER `public_id`;



# --- !Ups

ALTER TABLE Deposit ADD COLUMN `date_of_deposit` BIGINT(20) NOT NULL;



# --- !Ups

ALTER TABLE Deposit_Unit ADD COLUMN `currency` VARCHAR(3) DEFAULT 'EUR' AFTER `amount`;
ALTER TABLE Deposit ADD COLUMN `full_amount` DOUBLE(10,2) DEFAULT 0.0 AFTER `public_id`;
ALTER TABLE Deposit ADD COLUMN `currency` VARCHAR(3) DEFAULT 'EUR' AFTER `full_amount`;



# --- !Ups

ALTER TABLE Donation ADD COLUMN `crew` VARCHAR(36) NOT NULL AFTER `author`;


# --- !Ups

CREATE Table Household (
 id BIGINT(20) NOT NULL AUTO_INCREMENT,
 public_id VARCHAR(36) NOT NULL,
 PRIMARY KEY (id)
);

CREATE Table Place_Message (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  name VARCHAR(36) NOT NULL,
  token VARCHAR(36) NOT NULL,
  household_id BIGINT(20),
  PRIMARY KEY (id),
  CONSTRAINT FK_1_HOUSEHOLD FOREIGN KEY (household_id) REFERENCES Household(id) on DELETE CASCADE
);

CREATE Table Household_Version (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  public_id VARCHAR(36),
  iban VARCHAR(36) DEFAULT NULL,
  bic VARCHAR(36) DEFAULT NULL,
  created BIGINT(20) NOT NULL,
  updated BIGINT(20) NOT NULL,
  author VARCHAR(36),
  editor VARCHAR(36),
  amount DOUBLE NOT NULL,
  currency VARCHAR(5) NOT NULL DEFAULT 'EUR',
  reason_what VARCHAR(36) DEFAULT NULL,
  reason_wherefor VARCHAR(36) DEFAULT NULL,
  request BOOLEAN NOT NULL,
  volunteer_manager VARCHAR(36),
  employee VARCHAR(36),
  household_id BIGINT(20),
  PRIMARY KEY (id),
  CONSTRAINT FK_2_HOUSEHOLD FOREIGN KEY (household_id) REFERENCES Household(id) on DELETE CASCADE
);




# --- !Ups

ALTER TABLE `Household_Version` ADD COLUMN `crew_id` VARCHAR(36) NOT NULL AFTER `bic`;


# --- !Ups

CREATE TABLE Household_Version_History (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  public_id VARCHAR(36),
  iban VARCHAR(36) DEFAULT NULL,
  bic VARCHAR(36) DEFAULT NULL,
  crew_id VARCHAR(36) NOT NULL,
  created BIGINT(20) NOT NULL,
  updated BIGINT(20) NOT NULL,
  author VARCHAR(36),
  editor VARCHAR(36),
  amount DOUBLE NOT NULL,
  currency VARCHAR(5) NOT NULL DEFAULT 'EUR',
  reason_what VARCHAR(36) DEFAULT NULL,
  reason_wherefor VARCHAR(36) DEFAULT NULL,
  request BOOLEAN NOT NULL,
  volunteer_manager VARCHAR(36),
  employee VARCHAR(36),
  household_id BIGINT(20),
  PRIMARY KEY (id),
  CONSTRAINT FK_3_HOUSEHOLD FOREIGN KEY (household_id) REFERENCES Household(id) on DELETE CASCADE
);



# --- !Ups

ALTER TABLE `Donation` ADD COLUMN `norms` VARCHAR(36) NOT NULL AFTER `category`;


# --- !Ups
RENAME TABLE Donation TO Taking;
ALTER TABLE Deposit_Unit CHANGE donation_id taking_id BIGINT(20);
ALTER TABLE Source CHANGE donation_id taking_id BIGINT(20);
ALTER TABLE InvolvedSupporter CHANGE donation_id taking_id BIGINT(20);



# --- !Ups
ALTER TABLE `Taking` DROP COLUMN `norms`;
ALTER TABLE `Source` ADD COLUMN `norms` VARCHAR(36) NOT NULL;

# --- !Ups

ALTER TABLE `Source` ADD COLUMN `public_id` VARCHAR(36) NOT NULL AFTER `id`;

# --- !Ups
CREATE Table InvolvedCrew (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  taking_id BIGINT(20) NOT NULL,
  crew_id VARCHAR(36) NOT NULL,
  name VARCHAR(36) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_Crew_Taking FOREIGN KEY (taking_id) REFERENCES Taking(id) on DELETE CASCADE
);


# --- !Ups
ALTER TABLE `Taking` DROP COLUMN `crew`;
ALTER TABLE `InvolvedSupporter` ADD COLUMN `name` VARCHAR(36) NOT NULL;


# --- !Ups
ALTER TABLE `Deposit` ADD COLUMN `crew_name` VARCHAR(36) NOT NULL AFTER `crew`;


# --- !Ups
ALTER TABLE `Deposit` ADD COLUMN `confirmed_user_uuid` VARCHAR(36) AFTER `confirmed`;
ALTER TABLE `Deposit` ADD COLUMN `confirmed_user_name` VARCHAR(36) AFTER `confirmed_user_uuid`;
ALTER TABLE `Deposit` ADD COLUMN `supporter_name` VARCHAR(36) NOT NULL AFTER `supporter`;
ALTER TABLE `Deposit_Unit` ADD COLUMN `confirmed_user_uuid` VARCHAR(36) AFTER `confirmed`;
ALTER TABLE `Deposit_Unit` ADD COLUMN `confirmed_user_name` VARCHAR(36) AFTER `confirmed_user_uuid`;





# --- !Ups
ALTER TABLE `Source` ADD COLUMN `type_location` VARCHAR(36) AFTER `type_of_source`;
ALTER TABLE `Source` ADD COLUMN `type_contact_person` VARCHAR(36) AFTER `type_location`;
ALTER TABLE `Source` ADD COLUMN `type_email` VARCHAR(36) AFTER `type_contact_person`;
ALTER TABLE `Source` ADD COLUMN `type_address` VARCHAR(36) AFTER `type_email`;
ALTER TABLE `Source` ADD COLUMN `receipt` BOOLEAN AFTER `type_address`;



# --- !Ups
ALTER TABLE `Source` ADD COLUMN `description` VARCHAR(255) AFTER `category`;

