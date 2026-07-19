const jwt = require('jsonwebtoken');
require('dotenv').config();

const blacklistedTokens = new Set();

function invalidateToken(token) {
  blacklistedTokens.add(token);
}

function isTokenBlacklisted(token) {
  return blacklistedTokens.has(token);
}

function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ status: 401, message: 'Token tidak tersedia' });
  }

  if (isTokenBlacklisted(token)) {
    return res.status(401).json({ status: 401, message: 'Token sudah tidak berlaku' });
  }

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.user = decoded;
    next();
  } catch (err) {
    console.error('Token verification failed:', err.message);
    return res.status(401).json({ status: 401, message: 'Token tidak valid atau kedaluwarsa' });
  }
}

module.exports = { authenticateToken, invalidateToken, isTokenBlacklisted };
