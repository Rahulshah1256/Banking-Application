-- In-app notifications, support tickets and their conversation threads.

CREATE TABLE IF NOT EXISTS notifications (
  id            BIGINT NOT NULL AUTO_INCREMENT,
  user_id       BIGINT NOT NULL,
  type          VARCHAR(30) NOT NULL,
  channel       VARCHAR(20) NOT NULL,
  title         VARCHAR(150) NOT NULL,
  message       VARCHAR(1000) NOT NULL,
  is_read       BIT NOT NULL DEFAULT 0,
  read_at       DATETIME(6) NULL,
  created_at    DATETIME(6) NULL,
  updated_at    DATETIME(6) NULL,
  version       BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_notification_user (user_id),
  KEY idx_notification_user_read (user_id, is_read),
  CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS support_tickets (
  id                BIGINT NOT NULL AUTO_INCREMENT,
  ticket_reference  VARCHAR(40) NOT NULL,
  user_id           BIGINT NOT NULL,
  category          VARCHAR(20) NOT NULL,
  subject           VARCHAR(200) NOT NULL,
  description       VARCHAR(2000) NOT NULL,
  priority          VARCHAR(20) NOT NULL,
  status            VARCHAR(20) NOT NULL,
  resolved_at       DATETIME(6) NULL,
  created_at        DATETIME(6) NULL,
  updated_at        DATETIME(6) NULL,
  version           BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ticket_reference (ticket_reference),
  KEY idx_ticket_user (user_id),
  KEY idx_ticket_user_status (user_id, status),
  CONSTRAINT fk_ticket_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ticket_messages (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  ticket_id       BIGINT NOT NULL,
  sender_type     VARCHAR(20) NOT NULL,
  sender_user_id  BIGINT NULL,
  message         VARCHAR(2000) NOT NULL,
  created_at      DATETIME(6) NULL,
  updated_at      DATETIME(6) NULL,
  version         BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_ticket_message_ticket (ticket_id),
  CONSTRAINT fk_ticket_message_ticket FOREIGN KEY (ticket_id) REFERENCES support_tickets (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
