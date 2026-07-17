-- Cheque books and their individual cheque leaves.

CREATE TABLE IF NOT EXISTS cheque_books (
  id                     BIGINT NOT NULL AUTO_INCREMENT,
  book_reference_number  VARCHAR(40) NOT NULL,
  user_id                BIGINT NOT NULL,
  account_number         VARCHAR(50) NOT NULL,
  number_of_leaves       INT NOT NULL,
  start_cheque_number    BIGINT NOT NULL,
  end_cheque_number      BIGINT NOT NULL,
  status                 VARCHAR(20) NOT NULL,
  delivery_address       VARCHAR(500) NULL,
  requested_at           DATETIME(6) NOT NULL,
  issued_at              DATETIME(6) NULL,
  delivered_at           DATETIME(6) NULL,
  created_at             DATETIME(6) NULL,
  updated_at             DATETIME(6) NULL,
  version                BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cheque_book_reference (book_reference_number),
  KEY idx_cheque_book_user (user_id),
  KEY idx_cheque_book_account (account_number),
  CONSTRAINT fk_cheque_book_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS cheques (
  id                        BIGINT NOT NULL AUTO_INCREMENT,
  cheque_book_id            BIGINT NOT NULL,
  user_id                   BIGINT NOT NULL,
  account_number            VARCHAR(50) NOT NULL,
  cheque_number             BIGINT NOT NULL,
  status                    VARCHAR(20) NOT NULL,
  stop_reason               VARCHAR(255) NULL,
  stopped_at                DATETIME(6) NULL,
  positive_pay_registered   BIT NOT NULL DEFAULT 0,
  positive_pay_amount       DOUBLE NULL,
  positive_pay_payee        VARCHAR(150) NULL,
  positive_pay_date         DATE NULL,
  created_at                DATETIME(6) NULL,
  updated_at                DATETIME(6) NULL,
  version                   BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cheque_number (cheque_number),
  KEY idx_cheque_book (cheque_book_id),
  KEY idx_cheque_user (user_id),
  KEY idx_cheque_user_status (user_id, status),
  CONSTRAINT fk_cheque_book FOREIGN KEY (cheque_book_id) REFERENCES cheque_books (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
