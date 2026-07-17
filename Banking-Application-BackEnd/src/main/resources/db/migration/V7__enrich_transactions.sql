-- Enrich transactions with reference number, transfer mode/type, description,
-- channel and initiator for a production-grade money-movement audit trail.

ALTER TABLE transactions
  ADD COLUMN reference_number     VARCHAR(40)  NULL,
  ADD COLUMN transfer_mode        VARCHAR(20)  NULL,
  ADD COLUMN transaction_type     VARCHAR(20)  NULL,
  ADD COLUMN description          VARCHAR(255) NULL,
  ADD COLUMN channel              VARCHAR(20)  NULL,
  ADD COLUMN initiated_by_user_id BIGINT       NULL;

-- Backfill legacy rows so the new unique/reference contract holds.
UPDATE transactions SET reference_number = CONCAT('TXN', LPAD(id, 12, '0')) WHERE reference_number IS NULL;
UPDATE transactions SET transfer_mode = 'WITHIN_BANK' WHERE transfer_mode IS NULL;
UPDATE transactions SET transaction_type = 'TRANSFER' WHERE transaction_type IS NULL;

ALTER TABLE transactions
  ADD CONSTRAINT uk_transactions_reference UNIQUE (reference_number);

CREATE INDEX idx_txn_from ON transactions (fromaccount);
CREATE INDEX idx_txn_to ON transactions (toaccount);
CREATE INDEX idx_txn_date ON transactions (transactiondate);

ALTER TABLE transactions
  ADD CONSTRAINT fk_txn_initiator FOREIGN KEY (initiated_by_user_id) REFERENCES users (id);
