-- UPI virtual payment addresses (VPAs) mapped to customer accounts.

CREATE TABLE IF NOT EXISTS upi_handles (
  id                   BIGINT NOT NULL AUTO_INCREMENT,
  vpa                  VARCHAR(100) NOT NULL,
  account_number       VARCHAR(50) NOT NULL,
  user_id              BIGINT NOT NULL,
  is_primary           BIT NOT NULL DEFAULT 0,
  active               BIT NOT NULL DEFAULT 1,
  created_at           DATETIME(6) NULL,
  updated_at           DATETIME(6) NULL,
  version              BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_upi_vpa (vpa),
  KEY idx_upi_user (user_id),
  KEY idx_upi_account (account_number),
  CONSTRAINT fk_upi_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
