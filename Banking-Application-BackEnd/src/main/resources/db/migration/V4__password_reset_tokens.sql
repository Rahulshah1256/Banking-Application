-- Single-use, hashed password-reset tokens for the forgot/reset-password flow.

CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id          BIGINT NOT NULL AUTO_INCREMENT,
  token_hash  VARCHAR(255) NOT NULL,
  user_id     BIGINT NOT NULL,
  expiry_date DATETIME(6) NOT NULL,
  used        BIT NOT NULL,
  created_at  DATETIME(6) NULL,
  updated_at  DATETIME(6) NULL,
  version     BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_reset_token_hash (token_hash),
  KEY idx_reset_user (user_id),
  KEY idx_reset_expiry (expiry_date),
  CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
