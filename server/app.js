const express = require('express');
const cors = require('cors');
require('dotenv').config();

const { authenticateToken } = require('./middleware/auth');
const authRoutes = require('./routes/authRoutes');
const productRoutes = require('./routes/productRoutes');
const categoryRoutes = require('./routes/categoryRoutes');
const transactionRoutes = require('./routes/transactionRoutes');
const paymentRoutes = require('./routes/paymentRoutes');

const app = express();

app.use(cors());
app.use(express.json({ limit: '10mb' }));

app.use('/api', authRoutes);
app.use('/api/products', authenticateToken, productRoutes);
app.use('/api/categories', authenticateToken, categoryRoutes);
app.use('/api/transactions', authenticateToken, transactionRoutes);
app.use('/api/payment-methods', authenticateToken, paymentRoutes);

app.use((req, res) => {
  res.status(404).json({ status: 404, message: 'Endpoint tidak ditemukan' });
});

app.use((err, req, res, next) => {
  console.error('Unhandled error:', err);
  res.status(500).json({ status: 500, message: 'Terjadi kesalahan server' });
});

module.exports = app;
