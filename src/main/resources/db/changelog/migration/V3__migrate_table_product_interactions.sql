--
-- Name: product_interactions; Type: TABLE; Schema: public; Owner: postgres
--
CREATE TABLE product_interactions
(
    user_id    BIGINT  NOT NULL,
    product_id BIGINT  NOT NULL,
    is_like    BOOLEAN NOT NULL,
    PRIMARY KEY (user_id, product_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES product (id)
)