const express = require('express');
const productModel = require('../models/productModel');

const router = express.Router();

router.get('/', async (req, res) => {
  try {
    const products = await productModel.findAll();
    res.json({
      status: 200,
      message: 'Daftar produk',
      data: products,
    });
  } catch (err) {
    res.status(500).json({ status: 500, message: 'Gagal memuat produk' });
  }
});

router.get('/:id', async (req, res) => {
  try {
    const product = await productModel.findById(req.params.id);
    if (!product) {
      return res.status(404).json({ status: 404, message: 'Produk tidak ditemukan' });
    }
    res.json({ status: 200, data: product });
  } catch (err) {
    res.status(500).json({ status: 500, message: 'Gagal memuat produk' });
  }
});

module.exports = router;
