-- Debit/credit cards and their action history.

CREATE TABLE IF NOT EXISTS cards (
  id                    BIGINT NOT NULL AUTO_INCREMENT,
  card_number           VARCHAR(19) NOT NULL,
  card_holder_name      VARCHAR(150) NOT NULL,
  account_number        VARCHAR(50) NOT NULL,
  user_id               BIGINT NOT NULL,
  card_type             VARCHAR(20) NOT NULL,
  network               VARCHAR(20) NOT NULL,
  status                VARCHAR(20) NOT NULL,
  expiry_month          INT NOT NULL,
  expiry_year           INT NOT NULL,
  cvv_hash              VARCHAR(100) NOT NULL,
  pin_hash              VARCHAR(100) NULL,
  international_enabled  BIT NOT NULL DEFAULT 0,
  online_enabled        BIT NOT NULL DEFAULT 1,
  contactless_enabled   BIT NOT NULL DEFAULT 1,
  atm_daily_limit       DOUBLE NOT NULL,
  pos_daily_limit       DOUBLE NOT NULL,
  online_daily_limit    DOUBLE NOT NULL,
  blocked_reason        VARCHAR(255) NULL,
  issued_at             DATETIME(6) NOT NULL,
  created_at            DATETIME(6) NULL,
  updated_at            DATETIME(6) NULL,
  version               BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_card_number (card_number),
  KEY idx_card_user (user_id),
  KEY idx_card_account (account_number),
  CONSTRAINT fk_card_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS card_history (
  id          BIGINT NOT NULL AUTO_INCREMENT,
  card_id     BIGINT NOT NULL,
  action      VARCHAR(30) NOT NULL,
  details     VARCHAR(255) NULL,
  created_at  DATETIME(6) NULL,
  updated_at  DATETIME(6) NULL,
  version     BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_card_history_card (card_id),
  CONSTRAINT fk_card_history_card FOREIGN KEY (card_id) REFERENCES cards (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
