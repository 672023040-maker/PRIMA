const express = require('express');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const { v4: uuidv4 } = require('uuid');

const app = express();
const PORT = 3000;
const JWT_SECRET = 'prima-secret-key-2024';

app.use(cors());
app.use(express.json());

// ==================== DATA STORE ====================

const users = [
  { id: 1, name: 'Kasir A', email: 'kasir@prima.com', password: 'kasir123', role: 'kasir' },
  { id: 2, name: 'Admin B', email: 'admin@prima.com', password: 'admin123', role: 'admin' },
  { id: 3, name: 'Owner C', email: 'owner@prima.com', password: 'owner123', role: 'owner' },
];

const products = [
  { id: 1, name: 'Kopi Hitam', price: 15000, category: 'Minuman', description: 'Kopi hitam pilihan' },
  { id: 2, name: 'Kopi Susu', price: 18000, category: 'Minuman', description: 'Kopi dengan susu segar' },
  { id: 3, name: 'Cappuccino', price: 22000, category: 'Minuman', description: 'Cappuccino klasik' },
  { id: 4, name: 'Matcha Latte', price: 25000, category: 'Minuman', description: 'Matcha asli Jepang' },
  { id: 5, name: 'Nasi Goreng', price: 28000, category: 'Makanan', description: 'Nasi goreng spesial' },
  { id: 6, name: 'Mie Goreng', price: 25000, category: 'Makanan', description: 'Mie goreng pedas' },
  { id: 7, name: 'French Fries', price: 18000, category: 'Makanan', description: 'Kentang goreng renyah' },
  { id: 8, name: 'Chicken Wings', price: 32000, category: 'Makanan', description: 'Sayap ayam crispy' },
  { id: 9, name: 'Air Mineral', price: 5000, category: 'Minuman', description: 'Air mineral kemasan' },
  { id: 10, name: 'Jus Jeruk', price: 15000, category: 'Minuman', description: 'Jus jeruk segar' },
];

let transactions = [];
let transactionDetails = [];
let nextTransactionId = 1;
let nextDetailId = 1;

// ==================== MIDDLEWARE ====================

function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ status: 401, message: 'Token tidak tersedia' });
  }

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = decoded;
    next();
  } catch (err) {
    return res.status(401).json({ status: 401, message: 'Token tidak valid' });
  }
}

// ==================== AUTH ENDPOINTS ====================

app.post('/api/login', (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(422).json({ status: 422, message: 'Email dan password wajib diisi' });
  }

  const user = users.find(u => u.email === email && u.password === password);

  if (!user) {
    return res.status(401).json({ status: 401, message: 'Kredensial tidak sesuai' });
  }

  const token = jwt.sign(
    { id: user.id, email: user.email, role: user.role },
    JWT_SECRET,
    { expiresIn: '24h' }
  );

  res.json({
    status: 200,
    message: 'Login berhasil',
    token,
    user: {
      id: user.id,
      name: user.name,
      email: user.email,
      role: user.role,
    },
  });
});

app.post('/api/logout', authenticateToken, (req, res) => {
  res.json({ status: 200, message: 'Logout berhasil' });
});

// ==================== PRODUCT ENDPOINTS ====================

app.get('/api/products', authenticateToken, (req, res) => {
  res.json({
    status: 200,
    message: 'Daftar produk',
    data: products,
  });
});

// ==================== TRANSACTION ENDPOINTS ====================

app.post('/api/transactions', authenticateToken, (req, res) => {
  const { kasir_id } = req.body;

  if (!kasir_id) {
    return res.status(422).json({ status: 422, message: 'kasir_id wajib diisi' });
  }

  const kasir = users.find(u => u.id === kasir_id);

  const transaction = {
    id: nextTransactionId++,
    transaction_code: `TRX-${Date.now()}-${Math.random().toString(36).substr(2, 5).toUpperCase()}`,
    kasir_id,
    kasir_name: kasir ? kasir.name : 'Unknown',
    total: 0,
    status: 'active',
    created_at: new Date().toISOString(),
    details: [],
  };

  transactions.push(transaction);

  res.status(201).json({
    status: 201,
    message: 'Transaksi berhasil dibuat',
    data: transaction,
  });
});

app.post('/api/transactions/:id/details', authenticateToken, (req, res) => {
  const transactionId = parseInt(req.params.id);
  const { product_id, quantity } = req.body;

  if (!product_id || !quantity) {
    return res.status(422).json({ status: 422, message: 'product_id dan quantity wajib diisi' });
  }

  const transaction = transactions.find(t => t.id === transactionId);
  if (!transaction) {
    return res.status(404).json({ status: 404, message: 'Transaksi tidak ditemukan' });
  }

  const product = products.find(p => p.id === product_id);
  if (!product) {
    return res.status(404).json({ status: 404, message: 'Produk tidak ditemukan' });
  }

  const subtotal = product.price * quantity;

  const detail = {
    id: nextDetailId++,
    transaction_id: transactionId,
    product_id,
    product_name: product.name,
    quantity,
    price: product.price,
    subtotal,
  };

  transactionDetails.push(detail);

  // Update transaction total
  transaction.total += subtotal;
  transaction.details = transactionDetails.filter(d => d.transaction_id === transactionId);

  res.status(201).json({
    status: 201,
    message: 'Detail transaksi berhasil ditambahkan',
    data: detail,
  });
});

app.get('/api/transactions', authenticateToken, (req, res) => {
  const user = req.user;
  let result = [...transactions];

  // Kasir only sees their own transactions
  if (user.role === 'kasir') {
    result = result.filter(t => t.kasir_id === user.id);
  }

  // Attach details to each transaction
  result = result.map(t => ({
    ...t,
    details: transactionDetails.filter(d => d.transaction_id === t.id),
  }));

  res.json({
    status: 200,
    message: 'Daftar transaksi',
    data: result,
  });
});

// ==================== START SERVER ====================

app.listen(PORT, '0.0.0.0', () => {
  console.log(`PRIMA API Server running on http://0.0.0.0:${PORT}`);
  console.log('Test accounts:');
  console.log('  Kasir : kasir@prima.com / kasir123');
  console.log('  Admin : admin@prima.com / admin123');
  console.log('  Owner : owner@prima.com / owner123');
});
