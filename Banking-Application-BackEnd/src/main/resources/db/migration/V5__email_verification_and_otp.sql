-- Email verification + OTP-based two-factor login support.

ALTER TABLE users
  ADD COLUMN email_verified BIT NOT NULL DEFAULT 0;

-- Treat pre-existing active accounts as already email-verified (non-breaking).
UPDATE users SET email_verified = 1 WHERE status = 1;

CREATE TABLE IF NOT EXISTS email_verification_tokens (
  id          BIGINT NOT NULL AUTO_INCREMENT,
  token_hash  VARCHAR(255) NOT NULL,
  user_id     BIGINT NOT NULL,
  expiry_date DATETIME(6) NOT NULL,
  used        BIT NOT NULL,
  created_at  DATETIME(6) NULL,
  updated_at  DATETIME(6) NULL,
  version     BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_email_verif_hash (token_hash),
  KEY idx_email_verif_user (user_id),
  KEY idx_email_verif_expiry (expiry_date),
  CONSTRAINT fk_email_verif_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS login_otps (
  id          BIGINT NOT NULL AUTO_INCREMENT,
  otp_hash    VARCHAR(255) NOT NULL,
  user_id     BIGINT NOT NULL,
  expiry_date DATETIME(6) NOT NULL,
  consumed    BIT NOT NULL,
  attempts    INT NOT NULL DEFAULT 0,
  created_at  DATETIME(6) NULL,
  updated_at  DATETIME(6) NULL,
  version     BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_login_otp_user (user_id),
  KEY idx_login_otp_expiry (expiry_date),
  CONSTRAINT fk_login_otp_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
