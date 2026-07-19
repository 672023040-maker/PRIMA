CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('kasir', 'admin', 'owner')),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE payment_methods (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    category_id INTEGER REFERENCES categories(id),
    name VARCHAR(100) NOT NULL,
    price DECIMAL(12,2) NOT NULL CHECK (price >= 0),
    description TEXT,
    image_url VARCHAR(500),
    stock INTEGER DEFAULT 0 CHECK (stock >= 0),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    transaction_code VARCHAR(30) UNIQUE NOT NULL,
    kasir_id INTEGER REFERENCES users(id),
    payment_method_id INTEGER REFERENCES payment_methods(id),
    total DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (total >= 0),
    amount_paid DECIMAL(12,2),
    change_amount DECIMAL(12,2),
    notes TEXT,
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'completed', 'cancelled')),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE transaction_details (
    id SERIAL PRIMARY KEY,
    transaction_id INTEGER REFERENCES transactions(id) ON DELETE CASCADE,
    product_id INTEGER REFERENCES products(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price DECIMAL(12,2) NOT NULL CHECK (price >= 0),
    subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal >= 0),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_active ON products(is_active);
CREATE INDEX idx_trx_kasir ON transactions(kasir_id);
CREATE INDEX idx_trx_code ON transactions(transaction_code);
CREATE INDEX idx_trx_date ON transactions(created_at);
CREATE INDEX idx_trx_payment ON transactions(payment_method_id);
CREATE INDEX idx_trxdetail_trx ON transaction_details(transaction_id);
CREATE INDEX idx_trxdetail_product ON transaction_details(product_id);
