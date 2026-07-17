-- Refresh-token store (hashed) and access-token (jti) blacklist for logout/revocation.

CREATE TABLE IF NOT EXISTS refresh_tokens (
  id          BIGINT NOT NULL AUTO_INCREMENT,
  token_hash  VARCHAR(255) NOT NULL,
  user_id     BIGINT NOT NULL,
  expiry_date DATETIME(6) NOT NULL,
  revoked     BIT NOT NULL,
  created_at  DATETIME(6) NULL,
  updated_at  DATETIME(6) NULL,
  version     BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_refresh_token_hash (token_hash),
  KEY idx_refresh_user (user_id),
  KEY idx_refresh_expiry (expiry_date),
  CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS token_blacklist (
  id          BIGINT NOT NULL AUTO_INCREMENT,
  jti         VARCHAR(64) NOT NULL,
  expiry_date DATETIME(6) NOT NULL,
  created_at  DATETIME(6) NULL,
  updated_at  DATETIME(6) NULL,
  version     BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_blacklist_jti (jti),
  KEY idx_blacklist_expiry (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
