# --- !Ups

DROP TABLE Deposit_Unit;
CREATE Table Deposit_Unit (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  public_id VARCHAR(36) NOT NULL,
  `confirmed` BIGINT(20) DEFAULT NULL,
  amount DOUBLE(10,2) NOT NULL,
  created BIGINT(20) NOT NULL,
  deposit_id BIGINT(20) NOT NULL,
  `donation_id` BIGINT(20) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT public_id_constraint UNIQUE (public_id),
  CONSTRAINT FK_DEPOSIT FOREIGN KEY (deposit_id) REFERENCES Deposit(id) on DELETE CASCADE,
  CONSTRAINT FK_DONATION FOREIGN KEY (donation_id) REFERENCES Donation(id) on DELETE CASCADE
);

# --- !Downs

DROP TABLE Deposit_Unit;
CREATE Table Deposit_Unit (
  id BIGINT(20) NOT NULL,
  public_id VARCHAR(36) NOT NULL,
  `confirmed` BIGINT(20) DEFAULT NULL,
  amount VARCHAR(36) NOT NULL,
  created BIGINT(20) NOT NULL,
  updated BIGINT(20) NOT NULL,
  deposit_id BIGINT(20) NOT NULL,
  `donation_id` BIGINT(20) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT public_id_constraint UNIQUE (public_id),
  CONSTRAINT FK_DEPOSIT FOREIGN KEY (deposit_id) REFERENCES Deposit(id) on DELETE CASCADE,
  CONSTRAINT FK_DONATION FOREIGN KEY (donation_id) REFERENCES Donation(id) on DELETE CASCADE
);