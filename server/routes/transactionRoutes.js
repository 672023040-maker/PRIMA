const express = require('express');
const { v4: uuidv4 } = require('uuid');
const { pool } = require('../config/database');
const transactionModel = require('../models/transactionModel');
const productModel = require('../models/productModel');

const router = express.Router();

function requireOwnTransaction(req, res, next) {
  if (req.user.role === 'kasir' && req.user.id !== req.transaction?.kasir_id) {
    return res.status(403).json({ status: 403, message: 'Anda tidak memiliki akses ke transaksi ini' });
  }
  next();
}

router.post('/', async (req, res) => {
  const kasirId = req.user.id;
  const transactionCode = `TRX-${uuidv4().substring(0, 8).toUpperCase()}`;

  try {
    const transaction = await transactionModel.create(kasirId, transactionCode);

    res.status(201).json({
      status: 201,
      message: 'Transaksi berhasil dibuat',
      data: { ...transaction, details: [] },
    });
  } catch (err) {
    console.error('Gagal membuat transaksi:', err);
    res.status(500).json({ status: 500, message: 'Gagal membuat transaksi' });
  }
});

router.post('/:id/details', async (req, res) => {
  const transactionId = parseInt(req.params.id);
  const { product_id, quantity } = req.body;

  if (!product_id || !quantity) {
    return res.status(422).json({ status: 422, message: 'product_id dan quantity wajib diisi' });
  }

  if (typeof quantity !== 'number' || quantity <= 0 || !Number.isInteger(quantity)) {
    return res.status(422).json({ status: 422, message: 'quantity harus bilangan bulat positif' });
  }

  const client = await pool.connect();

  try {
    await client.query('BEGIN');

    const transactionRes = await client.query(
      `SELECT * FROM transactions WHERE id = $1 FOR UPDATE`,
      [transactionId]
    );
    const transaction = transactionRes.rows[0];
    if (!transaction) {
      await client.query('ROLLBACK');
      client.release();
      return res.status(404).json({ status: 404, message: 'Transaksi tidak ditemukan' });
    }

    if (transaction.status !== 'active') {
      await client.query('ROLLBACK');
      client.release();
      return res.status(422).json({ status: 422, message: 'Transaksi sudah selesai atau dibatalkan' });
    }

    if (req.user.role === 'kasir' && req.user.id !== transaction.kasir_id) {
      await client.query('ROLLBACK');
      client.release();
      return res.status(403).json({ status: 403, message: 'Anda tidak memiliki akses ke transaksi ini' });
    }

    const productRes = await client.query(
      'SELECT * FROM products WHERE id = $1 FOR UPDATE',
      [product_id]
    );
    const product = productRes.rows[0];
    if (!product) {
      await client.query('ROLLBACK');
      client.release();
      return res.status(404).json({ status: 404, message: 'Produk tidak ditemukan' });
    }

    if (product.stock < quantity) {
      await client.query('ROLLBACK');
      client.release();
      return res.status(422).json({
        status: 422,
        message: `Stok ${product.name} tidak mencukupi. Stok tersedia: ${product.stock}`,
      });
    }

    const subtotal = product.price * quantity;
    const detailRes = await client.query(
      `INSERT INTO transaction_details (transaction_id, product_id, quantity, price, subtotal)
       VALUES ($1, $2, $3, $4, $5) RETURNING *`,
      [transactionId, product_id, quantity, product.price, subtotal]
    );

    await client.query(
      'UPDATE transactions SET total = total + $1, updated_at = NOW() WHERE id = $2',
      [subtotal, transactionId]
    );

    await client.query(
      'UPDATE products SET stock = stock - $1, updated_at = NOW() WHERE id = $2',
      [quantity, product_id]
    );

    await client.query('COMMIT');
    client.release();

    res.status(201).json({
      status: 201,
      message: 'Detail transaksi berhasil ditambahkan',
      data: { ...detailRes.rows[0], product_name: product.name },
    });
  } catch (err) {
    await client.query('ROLLBACK').catch(() => {});
    client.release();
    console.error('Gagal menambah detail transaksi:', err);
    res.status(500).json({ status: 500, message: 'Gagal menambah detail transaksi' });
  }
});

router.post('/:id/complete', async (req, res) => {
  const transactionId = parseInt(req.params.id);
  const { payment_method_id, amount_paid } = req.body;

  if (!payment_method_id || !amount_paid) {
    return res.status(422).json({ status: 422, message: 'payment_method_id dan amount_paid wajib diisi' });
  }

  if (typeof amount_paid !== 'number' || amount_paid <= 0) {
    return res.status(422).json({ status: 422, message: 'Jumlah bayar harus lebih dari 0' });
  }

  const client = await pool.connect();

  try {
    await client.query('BEGIN');

    const transactionRes = await client.query(
      'SELECT * FROM transactions WHERE id = $1 FOR UPDATE',
      [transactionId]
    );
    const transaction = transactionRes.rows[0];
    if (!transaction) {
      await client.query('ROLLBACK');
      client.release();
      return res.status(404).json({ status: 404, message: 'Transaksi tidak ditemukan' });
    }

    if (transaction.status !== 'active') {
      await client.query('ROLLBACK');
      client.release();
      return res.status(422).json({ status: 422, message: 'Transaksi sudah selesai atau dibatalkan' });
    }

    if (req.user.role === 'kasir' && req.user.id !== transaction.kasir_id) {
      await client.query('ROLLBACK');
      client.release();
      return res.status(403).json({ status: 403, message: 'Anda tidak memiliki akses ke transaksi ini' });
    }

    const changeAmount = Math.round((amount_paid - transaction.total) * 100) / 100;
    if (changeAmount < 0) {
      await client.query('ROLLBACK');
      client.release();
      return res.status(422).json({ status: 422, message: 'Jumlah bayar kurang dari total' });
    }

    const updateRes = await client.query(
      `UPDATE transactions SET payment_method_id = $1, amount_paid = $2,
       change_amount = $3, status = 'completed', updated_at = NOW()
       WHERE id = $4 RETURNING *`,
      [payment_method_id, amount_paid, changeAmount, transactionId]
    );

    await client.query('COMMIT');
    client.release();

    res.json({
      status: 200,
      message: 'Transaksi berhasil diselesaikan',
      data: updateRes.rows[0],
    });
  } catch (err) {
    await client.query('ROLLBACK').catch(() => {});
    client.release();
    console.error('Gagal menyelesaikan transaksi:', err);
    res.status(500).json({ status: 500, message: 'Gagal menyelesaikan transaksi' });
  }
});

router.get('/', async (req, res) => {
  const user = req.user;

  try {
    let transactions;
    if (user.role === 'kasir') {
      transactions = await transactionModel.findByKasir(user.id);
    } else {
      transactions = await transactionModel.findAll();
    }

    res.json({
      status: 200,
      message: 'Daftar transaksi',
      data: transactions,
    });
  } catch (err) {
    console.error('Gagal memuat transaksi:', err);
    res.status(500).json({ status: 500, message: 'Gagal memuat transaksi' });
  }
});

module.exports = router;
