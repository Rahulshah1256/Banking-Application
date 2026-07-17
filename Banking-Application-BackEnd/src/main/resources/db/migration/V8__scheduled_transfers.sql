-- Scheduled and recurring transfer instructions executed by the platform scheduler.

CREATE TABLE IF NOT EXISTS scheduled_transfers (
  id                    BIGINT NOT NULL AUTO_INCREMENT,
  reference_number      VARCHAR(40) NOT NULL,
  from_account_number   VARCHAR(50) NOT NULL,
  to_account_number     VARCHAR(50) NOT NULL,
  amount                DOUBLE NOT NULL,
  transfer_mode         VARCHAR(20) NOT NULL,
  frequency             VARCHAR(20) NOT NULL,
  status                VARCHAR(20) NOT NULL,
  description           VARCHAR(255) NULL,
  next_run_date         DATE NOT NULL,
  last_run_at           DATETIME(6) NULL,
  executions_count      INT NOT NULL DEFAULT 0,
  last_error            VARCHAR(500) NULL,
  initiated_by_user_id  BIGINT NOT NULL,
  created_at            DATETIME(6) NULL,
  updated_at            DATETIME(6) NULL,
  version               BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_scheduled_transfer_reference (reference_number),
  KEY idx_scheduled_transfer_user (initiated_by_user_id),
  KEY idx_scheduled_transfer_due (status, next_run_date),
  CONSTRAINT fk_scheduled_transfer_user FOREIGN KEY (initiated_by_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
