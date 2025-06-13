CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    user_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    u_uuid VARCHAR(36) UNIQUE NOT NULL
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    p_name VARCHAR(100) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    qty INTEGER NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    p_uuid VARCHAR(36) UNIQUE NOT NULL,
    category VARCHAR(50)
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    order_date TIMESTAMP,
    order_code VARCHAR(8),
    total_price DOUBLE PRECISION,
    delivery_address VARCHAR(200)
);

CREATE TABLE order_products (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id),
    product_id INTEGER REFERENCES products(id),
    quantity INTEGER NOT NULL
);

CREATE TABLE carts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    product_id INTEGER REFERENCES products(id),
    quantity INTEGER NOT NULL,
    CONSTRAINT unique_cart_entry UNIQUE (user_id, product_id)
);


INSERT INTO products (p_name, price, qty, category, p_uuid) VALUES ('Golden Sourdough', 5.99, 20, 'Bread', 'f47ac10b-58cc-4372-a567-0e02b2c3d479');
INSERT INTO users (user_name, email, password, u_uuid) VALUES ('dara', 'dara@thegoldenbread.com', 'password123', '550e8400-e29b-41d4-a716-446655440000');
INSERT INTO products (p_name, price, qty, category, p_uuid) VALUES ('Almond Croissant', 3.49, 15, 'Pastry', 'a1b2c3d4-e5f6-4g7h-8i9j-0k1l2m3n4o5p');


CREATE INDEX idx_products_name ON products(p_name);
CREATE INDEX idx_products_category ON products(category);

CREATE INDEX IF NOT EXISTS idx_products_id ON products(id);