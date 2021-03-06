# Create Houshold Schema
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
  created BIGINT(20),
  updated BIGINT(20),
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


# --- !Downs

DROP Table Place_Message;
DROP Table Household_Version;
DROP Table Household;
