ALTER TABLE product_interactions ADD COLUMN delete_attempts INT DEFAULT 0;
CREATE INDEX idx_interactions_delete_tracking ON product_interactions (is_deleted, delete_attempts);