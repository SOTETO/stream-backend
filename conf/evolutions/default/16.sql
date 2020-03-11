
# --- !Ups
CREATE Table InvolvedCrew (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  taking_id BIGINT(20) NOT NULL,
  crew_id VARCHAR(36) NOT NULL,
  name VARCHAR(36) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_Crew_Taking FOREIGN KEY (taking_id) REFERENCES Taking(id) on DELETE CASCADE
);

# --- !Downs

DROP TABLE InvolvedCrew;
