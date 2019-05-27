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

# --- !Downs

DROP TABLE Source;
DROP TABLE InvolvedSupporter;
DROP TABLE Donation;