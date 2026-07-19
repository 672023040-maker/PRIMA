const express = require('express');
const jwt = require('jsonwebtoken');
const userModel = require('../models/userModel');
const { invalidateToken, isTokenBlacklisted } = require('../middleware/auth');
require('dotenv').config();

const router = express.Router();

router.post('/login', async (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(422).json({ status: 422, message: 'Email dan password wajib diisi' });
  }

  try {
    const user = await userModel.findByEmail(email);

    if (!user) {
      return res.status(401).json({ status: 401, message: 'Kredensial tidak sesuai' });
    }

    const isMatch = await userModel.comparePassword(password, user.password);
    if (!isMatch) {
      return res.status(401).json({ status: 401, message: 'Kredensial tidak sesuai' });
    }

    const token = jwt.sign(
      { id: user.id, email: user.email, role: user.role },
      process.env.JWT_SECRET,
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
  } catch (err) {
    console.error('Gagal login:', err);
    res.status(500).json({ status: 500, message: 'Terjadi kesalahan server' });
  }
});

router.post('/logout', (req, res) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];
  if (token) invalidateToken(token);
  res.json({ status: 200, message: 'Logout berhasil' });
});

module.exports = router;
