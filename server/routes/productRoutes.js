const express = require('express');
const productModel = require('../models/productModel');

const router = express.Router();

function requireAdmin(req, res, next) {
  if (req.user.role !== 'admin' && req.user.role !== 'owner') {
    return res.status(403).json({ status: 403, message: 'Akses ditolak. Hanya admin dan owner.' });
  }
  next();
}

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

router.post('/', requireAdmin, async (req, res) => {
  const { name, price, category, description } = req.body;

  if (!name || price === undefined || price === null) {
    return res.status(422).json({ status: 422, message: 'Nama dan harga produk wajib diisi' });
  }

  try {
    const product = await productModel.create(null, name, Number(price), description || '', null, 0);
    res.status(201).json({
      status: 201,
      message: 'Produk berhasil ditambahkan',
      data: product,
    });
  } catch (err) {
    res.status(500).json({ status: 500, message: 'Gagal menambahkan produk' });
  }
});

router.put('/:id', requireAdmin, async (req, res) => {
  const productId = parseInt(req.params.id);
  const { name, price, category, description } = req.body;

  if (!name || price === undefined || price === null) {
    return res.status(422).json({ status: 422, message: 'Nama dan harga produk wajib diisi' });
  }

  try {
    const existing = await productModel.findById(productId);
    if (!existing) {
      return res.status(404).json({ status: 404, message: 'Produk tidak ditemukan' });
    }

    const product = await productModel.update(
      productId,
      existing.category_id,
      name,
      Number(price),
      description || '',
      existing.image_url,
      existing.stock,
      existing.is_active
    );
    res.json({
      status: 200,
      message: 'Produk berhasil diperbarui',
      data: product,
    });
  } catch (err) {
    res.status(500).json({ status: 500, message: 'Gagal memperbarui produk' });
  }
});

router.delete('/:id', requireAdmin, async (req, res) => {
  const productId = parseInt(req.params.id);

  try {
    const existing = await productModel.findById(productId);
    if (!existing) {
      return res.status(404).json({ status: 404, message: 'Produk tidak ditemukan' });
    }

    await productModel.deleteProduct(productId);
    res.json({
      status: 200,
      message: 'Produk berhasil dihapus',
    });
  } catch (err) {
    res.status(500).json({ status: 500, message: 'Gagal menghapus produk' });
  }
});

module.exports = router;
