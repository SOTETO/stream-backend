# Deposit Schema

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

# --- !Downs

DROP TABLE Deposit_Unit;
DROP TABLE Deposit;
