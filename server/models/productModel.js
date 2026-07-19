const { query } = require('../config/database');

const findAll = async () => {
  const result = await query(`
    SELECT p.id, p.name, p.price, p.description, p.image_url, p.stock, p.is_active,
           c.id as category_id, c.name as category_name
    FROM products p
    LEFT JOIN categories c ON p.category_id = c.id
    WHERE p.is_active = TRUE
    ORDER BY p.id
  `);
  return result.rows;
};

const findById = async (id) => {
  const result = await query(`
    SELECT p.id, p.name, p.price, p.description, p.image_url, p.stock, p.is_active,
           c.id as category_id, c.name as category_name
    FROM products p
    LEFT JOIN categories c ON p.category_id = c.id
    WHERE p.id = $1
  `, [id]);
  return result.rows[0];
};

const create = async (categoryId, name, price, description, imageUrl, stock) => {
  const result = await query(
    'INSERT INTO products (category_id, name, price, description, image_url, stock) VALUES ($1, $2, $3, $4, $5, $6) RETURNING *',
    [categoryId, name, price, description, imageUrl, stock || 0]
  );
  return result.rows[0];
};

const update = async (id, categoryId, name, price, description, imageUrl, stock, isActive) => {
  const result = await query(
    `UPDATE products SET category_id = $1, name = $2, price = $3, description = $4,
     image_url = $5, stock = $6, is_active = $7, updated_at = NOW()
     WHERE id = $8 RETURNING *`,
    [categoryId, name, price, description, imageUrl, stock, isActive, id]
  );
  return result.rows[0];
};

const deleteProduct = async (id) => {
  await query('UPDATE products SET is_active = FALSE, updated_at = NOW() WHERE id = $1', [id]);
};

module.exports = { findAll, findById, create, update, deleteProduct };
