-- Login history, device tracking, and failed-login lockout support.

ALTER TABLE users
  ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0,
  ADD COLUMN lock_until DATETIME(6) NULL;

CREATE TABLE IF NOT EXISTS login_history (
  id                 BIGINT NOT NULL AUTO_INCREMENT,
  user_id            BIGINT NULL,
  attempted_username VARCHAR(150) NULL,
  successful         BIT NOT NULL,
  failure_reason     VARCHAR(100) NULL,
  ip_address         VARCHAR(64) NULL,
  user_agent         VARCHAR(512) NULL,
  device_id          VARCHAR(128) NULL,
  login_time         DATETIME(6) NOT NULL,
  created_at         DATETIME(6) NULL,
  updated_at         DATETIME(6) NULL,
  version            BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_login_history_user (user_id),
  KEY idx_login_history_time (login_time),
  CONSTRAINT fk_login_history_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS user_devices (
  id           BIGINT NOT NULL AUTO_INCREMENT,
  user_id      BIGINT NOT NULL,
  device_id    VARCHAR(128) NOT NULL,
  device_name  VARCHAR(255) NULL,
  ip_address   VARCHAR(64) NULL,
  last_used_at DATETIME(6) NOT NULL,
  trusted      BIT NOT NULL,
  created_at   DATETIME(6) NULL,
  updated_at   DATETIME(6) NULL,
  version      BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_device (user_id, device_id),
  KEY idx_user_device_user (user_id),
  CONSTRAINT fk_user_device_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
