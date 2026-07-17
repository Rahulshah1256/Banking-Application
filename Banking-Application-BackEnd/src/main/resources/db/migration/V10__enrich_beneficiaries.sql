-- Enrich beneficiaries with nickname, favourite flag and activation-delay window.

ALTER TABLE beneficiaries
  ADD COLUMN nickname       VARCHAR(100) NULL,
  ADD COLUMN favourite      BIT NOT NULL DEFAULT 0,
  ADD COLUMN activate_after DATETIME(6) NULL;

CREATE INDEX idx_beneficiary_status ON beneficiaries (status);
