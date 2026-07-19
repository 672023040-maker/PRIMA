const { query } = require('../config/database');

const findAll = async () => {
  const result = await query(
    'SELECT * FROM payment_methods WHERE is_active = TRUE ORDER BY id'
  );
  return result.rows;
};

const findById = async (id) => {
  const result = await query('SELECT * FROM payment_methods WHERE id = $1', [id]);
  return result.rows[0];
};

module.exports = { findAll, findById };
