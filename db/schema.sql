DROP TABLE IF EXISTS parts;
DROP TABLE IF EXISTS products;

CREATE TABLE products (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    external_product_id BIGINT NOT NULL UNIQUE,
    product_name VARCHAR(255) NOT NULL,
    category_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE parts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id BIGINT NOT NULL,
    external_part_number VARCHAR(100) NOT NULL,
    part_description VARCHAR(255) NOT NULL,
    original_retail_price NUMERIC(12, 2) NOT NULL,
    brand_name VARCHAR(100) NOT NULL,
    image_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_parts_product
       FOREIGN KEY (product_id)
           REFERENCES products(id)
);

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_parts_product_id
    ON parts(product_id);

CREATE INDEX idx_products_category_name
    ON products (LOWER(category_name));

CREATE INDEX idx_products_product_name_trgm
    ON products
    USING GIN (LOWER(product_name) gin_trgm_ops);

CREATE INDEX idx_parts_external_part_number_trgm
    ON parts
    USING GIN (LOWER(external_part_number) gin_trgm_ops);

CREATE INDEX idx_parts_part_description_trgm
    ON parts
    USING GIN (LOWER(part_description) gin_trgm_ops);

CREATE INDEX idx_parts_brand_name_trgm
    ON parts
    USING GIN (LOWER(brand_name) gin_trgm_ops);