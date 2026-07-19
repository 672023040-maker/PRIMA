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
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

app.use('/api', authRoutes);
app.use('/api/products', authenticateToken, productRoutes);
app.use('/api/categories', authenticateToken, categoryRoutes);
app.use('/api/transactions', authenticateToken, transactionRoutes);
app.use('/api/payment-methods', authenticateToken, paymentRoutes);

app.listen(PORT, '0.0.0.0', () => {
  console.log(`PRIMA API Server running on http://0.0.0.0:${PORT}`);
});
