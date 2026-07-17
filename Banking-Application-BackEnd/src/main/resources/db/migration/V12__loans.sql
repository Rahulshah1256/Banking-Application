-- Loans and their repayment ledger.

CREATE TABLE IF NOT EXISTS loans (
  id                        BIGINT NOT NULL AUTO_INCREMENT,
  loan_reference_number     VARCHAR(40) NOT NULL,
  user_id                   BIGINT NOT NULL,
  disbursement_account_number VARCHAR(50) NOT NULL,
  loan_type                 VARCHAR(20) NOT NULL,
  principal                 DOUBLE NOT NULL,
  annual_interest_rate      DOUBLE NOT NULL,
  tenure_months             INT NOT NULL,
  emi_amount                DOUBLE NOT NULL,
  outstanding_principal     DOUBLE NOT NULL,
  status                    VARCHAR(20) NOT NULL,
  emis_paid                 INT NOT NULL DEFAULT 0,
  next_emi_date             DATE NULL,
  applied_at                DATETIME(6) NOT NULL,
  disbursed_at              DATETIME(6) NULL,
  created_at                DATETIME(6) NULL,
  updated_at                DATETIME(6) NULL,
  version                   BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_loan_reference (loan_reference_number),
  KEY idx_loan_user (user_id),
  CONSTRAINT fk_loan_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS loan_repayments (
  id                    BIGINT NOT NULL AUTO_INCREMENT,
  loan_id               BIGINT NOT NULL,
  repayment_type        VARCHAR(20) NOT NULL,
  amount                DOUBLE NOT NULL,
  principal_component   DOUBLE NOT NULL,
  interest_component    DOUBLE NOT NULL,
  outstanding_after     DOUBLE NOT NULL,
  paid_at               DATETIME(6) NOT NULL,
  created_at            DATETIME(6) NULL,
  updated_at            DATETIME(6) NULL,
  version               BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_loan_repayment_loan (loan_id),
  CONSTRAINT fk_loan_repayment_loan FOREIGN KEY (loan_id) REFERENCES loans (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
