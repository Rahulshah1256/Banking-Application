-- Customer profile (1:1 with users), nominees, and KYC documents.

CREATE TABLE IF NOT EXISTS customer_profiles (
  id                     BIGINT NOT NULL AUTO_INCREMENT,
  user_id                BIGINT NOT NULL,
  date_of_birth          DATE NULL,
  gender                 VARCHAR(10) NULL,
  occupation             VARCHAR(100) NULL,
  annual_income          DOUBLE NULL,
  communication_address  VARCHAR(500) NULL,
  permanent_address      VARCHAR(500) NULL,
  alternate_phone        VARCHAR(20) NULL,
  profile_photo_path     VARCHAR(255) NULL,
  profile_photo_content_type VARCHAR(100) NULL,
  kyc_status             VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at             DATETIME(6) NULL,
  updated_at             DATETIME(6) NULL,
  version                BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_customer_profile_user (user_id),
  CONSTRAINT fk_customer_profile_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS nominees (
  id                BIGINT NOT NULL AUTO_INCREMENT,
  user_id           BIGINT NOT NULL,
  name              VARCHAR(150) NOT NULL,
  relationship      VARCHAR(50) NOT NULL,
  share_percentage  DOUBLE NOT NULL,
  date_of_birth     DATE NULL,
  phone             VARCHAR(20) NULL,
  address           VARCHAR(500) NULL,
  created_at        DATETIME(6) NULL,
  updated_at        DATETIME(6) NULL,
  version           BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_nominee_user (user_id),
  CONSTRAINT fk_nominee_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS kyc_documents (
  id                BIGINT NOT NULL AUTO_INCREMENT,
  user_id           BIGINT NOT NULL,
  document_type     VARCHAR(30) NOT NULL,
  document_number   VARCHAR(50) NULL,
  storage_path      VARCHAR(255) NOT NULL,
  content_type      VARCHAR(100) NULL,
  file_size         BIGINT NOT NULL DEFAULT 0,
  status            VARCHAR(20) NOT NULL,
  remarks           VARCHAR(255) NULL,
  uploaded_at       DATETIME(6) NOT NULL,
  verified_at       DATETIME(6) NULL,
  created_at        DATETIME(6) NULL,
  updated_at        DATETIME(6) NULL,
  version           BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_kyc_user (user_id),
  KEY idx_kyc_user_type (user_id, document_type),
  CONSTRAINT fk_kyc_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
