ALTER TABLE fine_rule
    ADD COLUMN reservation_hold_days INTEGER NOT NULL DEFAULT 3;

ALTER TABLE fine_rule
    ADD CONSTRAINT ck_fine_reservation_hold_days
    CHECK (reservation_hold_days BETWEEN 1 AND 30);

ALTER TABLE loan
    ADD COLUMN fine_rule_id UUID REFERENCES fine_rule(id);

CREATE INDEX idx_loan_fine_rule ON loan(fine_rule_id);
