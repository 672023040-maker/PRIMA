const express = require('express');
const transactionModel = require('../models/transactionModel');
const productModel = require('../models/productModel');

const router = express.Router();

const generateTransactionCode = () => {
  const timestamp = Date.now();
  const random = Math.random().toString(36).substr(2, 5).toUpperCase();
  return `TRX-${timestamp}-${random}`;
};

router.post('/', async (req, res) => {
  const { kasir_id } = req.body;

  if (!kasir_id) {
    return res.status(422).json({ status: 422, message: 'kasir_id wajib diisi' });
  }

  try {
    const transactionCode = generateTransactionCode();
    const transaction = await transactionModel.create(kasir_id, transactionCode);

    res.status(201).json({
      status: 201,
      message: 'Transaksi berhasil dibuat',
      data: { ...transaction, details: [] },
    });
  } catch (err) {
    res.status(500).json({ status: 500, message: 'Gagal membuat transaksi' });
  }
});

router.post('/:id/details', async (req, res) => {
  const transactionId = parseInt(req.params.id);
  const { product_id, quantity } = req.body;

  if (!product_id || !quantity) {
    return res.status(422).json({ status: 422, message: 'product_id dan quantity wajib diisi' });
  }

  try {
    const transaction = await transactionModel.findById(transactionId);
    if (!transaction) {
      return res.status(404).json({ status: 404, message: 'Transaksi tidak ditemukan' });
    }

    const product = await productModel.findById(product_id);
    if (!product) {
      return res.status(404).json({ status: 404, message: 'Produk tidak ditemukan' });
    }

    const detail = await transactionModel.addDetail(transactionId, product_id, quantity, product.price);

    res.status(201).json({
      status: 201,
      message: 'Detail transaksi berhasil ditambahkan',
      data: { ...detail, product_name: product.name },
    });
  } catch (err) {
    res.status(500).json({ status: 500, message: 'Gagal menambah detail transaksi' });
  }
});

router.post('/:id/complete', async (req, res) => {
  const transactionId = parseInt(req.params.id);
  const { payment_method_id, amount_paid } = req.body;

  if (!payment_method_id || !amount_paid) {
    return res.status(422).json({ status: 422, message: 'payment_method_id dan amount_paid wajib diisi' });
  }

  try {
    const transaction = await transactionModel.findById(transactionId);
    if (!transaction) {
      return res.status(404).json({ status: 404, message: 'Transaksi tidak ditemukan' });
    }

    const changeAmount = amount_paid - transaction.total;
    if (changeAmount < 0) {
      return res.status(422).json({ status: 422, message: 'Jumlah bayar kurang dari total' });
    }

    const updated = await transactionModel.updatePayment(
      transactionId, payment_method_id, amount_paid, changeAmount, 'completed'
    );

    res.json({
      status: 200,
      message: 'Transaksi berhasil diselesaikan',
      data: updated,
    });
  } catch (err) {
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
    res.status(500).json({ status: 500, message: 'Gagal memuat transaksi' });
  }
});

module.exports = router;
