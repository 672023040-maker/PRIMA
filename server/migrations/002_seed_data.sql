INSERT INTO categories (name) VALUES ('Minuman'), ('Makanan');

INSERT INTO payment_methods (name, description) VALUES
    ('Tunai', 'Pembayaran tunai'),
    ('Kartu Kredit', 'Visa, Mastercard, dll'),
    ('Kartu Debit', 'Kartu debit bank'),
    ('GoPay', 'GoPay'),
    ('OVO', 'OVO'),
    ('Dana', 'Dana');

INSERT INTO users (name, email, password, role) VALUES
    ('Kasir A', 'kasir@prima.com', '$2b$10$jnTfkijnwgZIkqqQpA/lm.23QdZYphUvVEx5308uMc//JKSN1szOW', 'kasir'),
    ('Admin B', 'admin@prima.com', '$2b$10$y1Yw9F.Lr.MzdKnKKWQ8feYCN5iUUE5MWKbSTLGB/Dc1tH/B0su3.', 'admin'),
    ('Owner C', 'owner@prima.com', '$2b$10$Cxr9xYAZM9Z1SLKbJlvb1uHnEqaPWyg/Ceje.jfiNUlC5.YsEWBHG', 'owner');

INSERT INTO products (category_id, name, price, description, stock) VALUES
    (1, 'Kopi Hitam', 15000, 'Kopi hitam pilihan', 100),
    (1, 'Kopi Susu', 18000, 'Kopi dengan susu segar', 100),
    (1, 'Cappuccino', 22000, 'Cappuccino klasik', 100),
    (1, 'Matcha Latte', 25000, 'Matcha asli Jepang', 100),
    (2, 'Nasi Goreng', 28000, 'Nasi goreng spesial', 50),
    (2, 'Mie Goreng', 25000, 'Mie goreng pedas', 50),
    (2, 'French Fries', 18000, 'Kentang goreng renyah', 75),
    (2, 'Chicken Wings', 32000, 'Sayap ayam crispy', 50),
    (1, 'Air Mineral', 5000, 'Air mineral kemasan', 200),
    (1, 'Jus Jeruk', 15000, 'Jus jeruk segar', 80);
