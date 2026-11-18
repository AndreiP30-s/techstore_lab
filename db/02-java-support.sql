BEGIN;
-- Последовательность артикулов и таблицы скидок. Выполняется после 01-schema.sql.
CREATE SEQUENCE product_article_seq AS integer START WITH 100001 INCREMENT BY 1;
SELECT setval('product_article_seq', GREATEST(100001, COALESCE((SELECT MAX(article)::bigint + 1 FROM product),100001)), false);
ALTER TABLE product ALTER COLUMN article SET DEFAULT nextval('product_article_seq');
ALTER SEQUENCE product_article_seq OWNED BY product.article;
CREATE TABLE discount_state (id integer PRIMARY KEY CHECK (id=1), active boolean NOT NULL DEFAULT false);
INSERT INTO discount_state(id,active) VALUES (1,false);
CREATE TABLE discount_backup (
    product_article integer PRIMARY KEY REFERENCES product(article),
    original_price numeric(9,2) NOT NULL CHECK (original_price>0)
);
COMMIT;
