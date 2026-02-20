CREATE INDEX IF NOT EXISTS idx_deleted_interactions
    ON product_interactions (product_id, user_id)
    WHERE is_deleted = TRUE;