const { query } = require('../config/database');

const findAll = async () => {
  const result = await query('SELECT * FROM categories ORDER BY id');
  return result.rows;
};

const findById = async (id) => {
  const result = await query('SELECT * FROM categories WHERE id = $1', [id]);
  return result.rows[0];
};

const findByName = async (name) => {
  const result = await query('SELECT * FROM categories WHERE name = $1', [name]);
  return result.rows[0];
};

const findOrCreateByName = async (name) => {
  const existing = await findByName(name);
  if (existing) return existing;
  const result = await query(
    'INSERT INTO categories (name) VALUES ($1) RETURNING *',
    [name]
  );
  return result.rows[0];
};

const create = async (name, description) => {
  const result = await query(
    'INSERT INTO categories (name, description) VALUES ($1, $2) RETURNING *',
    [name, description]
  );
  return result.rows[0];
};

const update = async (id, name, description) => {
  const result = await query(
    'UPDATE categories SET name = $1, description = $2, updated_at = NOW() WHERE id = $3 RETURNING *',
    [name, description, id]
  );
  return result.rows[0];
};

const deleteCategory = async (id) => {
  const result = await query('DELETE FROM categories WHERE id = $1 RETURNING *', [id]);
  return result.rows[0];
};

module.exports = { findAll, findById, findByName, findOrCreateByName, create, update, deleteCategory };
