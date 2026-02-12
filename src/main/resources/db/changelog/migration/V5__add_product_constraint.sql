ALTER TABLE product
ADD CONSTRAINT check_price_positive
    CHECK (price > 0),
ADD CONSTRAINT check_name_not_empty
    CHECK (char_length(trim(name)) > 0);

