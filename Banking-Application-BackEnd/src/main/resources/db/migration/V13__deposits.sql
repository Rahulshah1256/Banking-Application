-- Fixed and recurring deposits.

CREATE TABLE IF NOT EXISTS deposits (
  id                     BIGINT NOT NULL AUTO_INCREMENT,
  deposit_reference_number VARCHAR(40) NOT NULL,
  user_id                BIGINT NOT NULL,
  linked_account_number  VARCHAR(50) NOT NULL,
  deposit_type           VARCHAR(20) NOT NULL,
  principal              DOUBLE NOT NULL,
  installment_amount     DOUBLE NOT NULL DEFAULT 0,
  installments_paid      INT NOT NULL DEFAULT 0,
  annual_interest_rate   DOUBLE NOT NULL,
  tenure_months          INT NOT NULL,
  maturity_amount        DOUBLE NOT NULL,
  status                 VARCHAR(20) NOT NULL,
  auto_renew             BIT NOT NULL DEFAULT 0,
  opened_at              DATETIME(6) NOT NULL,
  maturity_date          DATE NOT NULL,
  closed_at              DATETIME(6) NULL,
  created_at             DATETIME(6) NULL,
  updated_at             DATETIME(6) NULL,
  version                BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_deposit_reference (deposit_reference_number),
  KEY idx_deposit_user (user_id),
  KEY idx_deposit_status_maturity (status, maturity_date),
  CONSTRAINT fk_deposit_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
