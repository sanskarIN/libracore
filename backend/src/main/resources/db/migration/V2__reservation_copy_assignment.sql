ALTER TABLE reservation
    ADD COLUMN assigned_copy_id UUID REFERENCES book_copy(id);

CREATE UNIQUE INDEX uq_active_reservation_copy
    ON reservation(assigned_copy_id)
    WHERE assigned_copy_id IS NOT NULL AND status = 'READY';

CREATE INDEX idx_reservation_assigned_copy
    ON reservation(assigned_copy_id);
