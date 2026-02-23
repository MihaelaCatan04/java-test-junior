ALTER TABLE product_interactions
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_active_interactions
    ON product_interactions (product_id)
    WHERE is_deleted = FALSE;