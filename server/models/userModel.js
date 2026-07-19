const { query } = require('../config/database');
const bcrypt = require('bcryptjs');

const findByEmail = async (email) => {
  const result = await query('SELECT * FROM users WHERE email = $1', [email]);
  return result.rows[0];
};

const findById = async (id) => {
  const result = await query('SELECT id, name, email, role, created_at, updated_at FROM users WHERE id = $1', [id]);
  return result.rows[0];
};

const create = async (name, email, password, role) => {
  const hashedPassword = await bcrypt.hash(password, 10);
  const result = await query(
    'INSERT INTO users (name, email, password, role) VALUES ($1, $2, $3, $4) RETURNING id, name, email, role, created_at, updated_at',
    [name, email, hashedPassword, role]
  );
  return result.rows[0];
};

const comparePassword = async (plainPassword, hashedPassword) => {
  return bcrypt.compare(plainPassword, hashedPassword);
};

module.exports = { findByEmail, findById, create, comparePassword };
