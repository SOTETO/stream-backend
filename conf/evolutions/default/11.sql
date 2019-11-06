# Create Household Version History
# --- !Ups

CREATE TABLE Household_Version_History (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  public_id VARCHAR(36),
  iban VARCHAR(36) DEFAULT NULL,
  bic VARCHAR(36) DEFAULT NULL,
  crew_id VARCHAR(36) NOT NULL,
  created BIGINT(20) DEFAULT UNIX_TIMESTAMP(),
  updated BIGINT(20) DEFAULT UNIX_TIMESTAMP(),
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

# --- !Downs

DROP TABLE Household_Version_History;