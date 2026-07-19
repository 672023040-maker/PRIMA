const express = require('express');
const categoryModel = require('../models/categoryModel');

const router = express.Router();

router.get('/', async (req, res) => {
  try {
    const categories = await categoryModel.findAll();
    res.json({
      status: 200,
      message: 'Daftar kategori',
      data: categories,
    });
  } catch (err) {
    res.status(500).json({ status: 500, message: 'Gagal memuat kategori' });
  }
});

router.get('/:id', async (req, res) => {
  try {
    const category = await categoryModel.findById(req.params.id);
    if (!category) {
      return res.status(404).json({ status: 404, message: 'Kategori tidak ditemukan' });
    }
    res.json({ status: 200, data: category });
  } catch (err) {
    res.status(500).json({ status: 500, message: 'Gagal memuat kategori' });
  }
});

module.exports = router;
