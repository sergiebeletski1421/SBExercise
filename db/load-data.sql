BEGIN;

DROP TABLE IF EXISTS tmp_products;
DROP TABLE IF EXISTS tmp_parts;

CREATE TEMP TABLE tmp_products (
    external_product_id TEXT,
    product_name TEXT,
    category_name TEXT
);

CREATE TEMP TABLE tmp_parts (
    external_part_number TEXT,
    part_description TEXT,
    external_product_id TEXT,
    original_retail_price TEXT,
    brand_name TEXT,
    image_url TEXT
);

COPY tmp_products (
    external_product_id,
    product_name,
    category_name)
FROM '/data/products.csv'
WITH (FORMAT CSV, HEADER TRUE);

COPY tmp_parts (
    external_part_number,
    part_description,
    external_product_id,
    original_retail_price,
    brand_name,
    image_url)
FROM '/data/parts.csv'
WITH (FORMAT CSV, HEADER TRUE);

INSERT INTO products (
    external_product_id,
    product_name,
    category_name
)
SELECT
    external_product_id::BIGINT,
    TRIM(product_name),
    TRIM(category_name)
FROM tmp_products;

INSERT INTO parts (
    product_id,
    external_part_number,
    part_description,
    original_retail_price,
    brand_name,
    image_url
)
SELECT
    p.id,
    TRIM(sp.external_part_number),
    TRIM(sp.part_description),
    sp.original_retail_price::NUMERIC(12, 2),
    TRIM(sp.brand_name),
    NULLIF(TRIM(sp.image_url), '')
FROM tmp_parts sp
         JOIN products p
              ON p.external_product_id = sp.external_product_id::BIGINT;

COMMIT;