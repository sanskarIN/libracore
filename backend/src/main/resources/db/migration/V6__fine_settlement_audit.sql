ALTER TABLE fine_charge
    ADD COLUMN settled_by_user_id UUID REFERENCES app_user(id),
    ADD COLUMN settlement_note VARCHAR(500);

CREATE INDEX idx_fine_settled_by ON fine_charge(settled_by_user_id);
