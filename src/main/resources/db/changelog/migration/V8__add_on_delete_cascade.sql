ALTER TABLE product_interactions
DROP CONSTRAINT IF EXISTS fk_product;

ALTER TABLE product_interactions
    ADD CONSTRAINT fk_product
        FOREIGN KEY (product_id)
            REFERENCES product(id)
            ON DELETE CASCADE;