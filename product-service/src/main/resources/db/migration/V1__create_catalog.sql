CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    parent_id BIGINT REFERENCES categories (id)
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(4000),
    price NUMERIC(19, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    category_id BIGINT NOT NULL REFERENCES categories (id),
    image_url VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
