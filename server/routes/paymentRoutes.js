const express = require('express');
const paymentModel = require('../models/paymentModel');

const router = express.Router();

router.get('/', async (req, res) => {
  try {
    const methods = await paymentModel.findAll();
    res.json({
      status: 200,
      message: 'Daftar metode pembayaran',
      data: methods,
    });
  } catch (err) {
    console.error('Gagal memuat metode pembayaran:', err);
    res.status(500).json({ status: 500, message: 'Gagal memuat metode pembayaran' });
  }
});

module.exports = router;
