-- Add JPA auditing columns (created_at, updated_at, optimistic-lock version)
-- to the core business tables, plus indexes on frequently-queried columns.

ALTER TABLE users
  ADD COLUMN created_at DATETIME(6) NULL,
  ADD COLUMN updated_at DATETIME(6) NULL,
  ADD COLUMN version    BIGINT NOT NULL DEFAULT 0;

ALTER TABLE accounts
  ADD COLUMN created_at DATETIME(6) NULL,
  ADD COLUMN updated_at DATETIME(6) NULL,
  ADD COLUMN version    BIGINT NOT NULL DEFAULT 0;

ALTER TABLE beneficiaries
  ADD COLUMN created_at DATETIME(6) NULL,
  ADD COLUMN updated_at DATETIME(6) NULL,
  ADD COLUMN version    BIGINT NOT NULL DEFAULT 0;

ALTER TABLE transactions
  ADD COLUMN created_at DATETIME(6) NULL,
  ADD COLUMN updated_at DATETIME(6) NULL,
  ADD COLUMN version    BIGINT NOT NULL DEFAULT 0;

CREATE INDEX idx_users_status         ON users (status);
CREATE INDEX idx_accounts_status      ON accounts (status);
CREATE INDEX idx_accounts_number      ON accounts (accountnumber);
CREATE INDEX idx_beneficiaries_status ON beneficiaries (status);
CREATE INDEX idx_transactions_from    ON transactions (fromaccount);
CREATE INDEX idx_transactions_to      ON transactions (toaccount);
CREATE INDEX idx_transactions_date    ON transactions (transactiondate);
