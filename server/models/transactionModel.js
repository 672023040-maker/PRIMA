const { query } = require('../config/database');

const create = async (kasirId, transactionCode) => {
  const result = await query(
    `INSERT INTO transactions (transaction_code, kasir_id, total, status)
     VALUES ($1, $2, 0, 'active') RETURNING *`,
    [transactionCode, kasirId]
  );
  return result.rows[0];
};

const addDetail = async (transactionId, productId, quantity, price) => {
  const subtotal = price * quantity;
  const result = await query(
    `INSERT INTO transaction_details (transaction_id, product_id, quantity, price, subtotal)
     VALUES ($1, $2, $3, $4, $5) RETURNING *`,
    [transactionId, productId, quantity, price, subtotal]
  );

  await query(
    'UPDATE transactions SET total = total + $1, updated_at = NOW() WHERE id = $2',
    [subtotal, transactionId]
  );

  return result.rows[0];
};

const findByKasir = async (kasirId) => {
  const transactions = await query(
    `SELECT t.*, u.name as kasir_name, pm.name as payment_method_name
     FROM transactions t
     LEFT JOIN users u ON t.kasir_id = u.id
     LEFT JOIN payment_methods pm ON t.payment_method_id = pm.id
     WHERE t.kasir_id = $1
     ORDER BY t.created_at DESC`,
    [kasirId]
  );

  if (transactions.rows.length === 0) return [];

  const ids = transactions.rows.map(t => t.id);
  const details = await query(
    `SELECT td.*, p.name as product_name
     FROM transaction_details td
     LEFT JOIN products p ON td.product_id = p.id
     WHERE td.transaction_id = ANY($1::int[])
     ORDER BY td.id`,
    [ids]
  );

  const detailsByTrx = {};
  for (const d of details.rows) {
    if (!detailsByTrx[d.transaction_id]) detailsByTrx[d.transaction_id] = [];
    detailsByTrx[d.transaction_id].push(d);
  }

  for (const t of transactions.rows) {
    t.details = detailsByTrx[t.id] || [];
  }

  return transactions.rows;
};

const findAll = async () => {
  const transactions = await query(
    `SELECT t.*, u.name as kasir_name, pm.name as payment_method_name
     FROM transactions t
     LEFT JOIN users u ON t.kasir_id = u.id
     LEFT JOIN payment_methods pm ON t.payment_method_id = pm.id
     ORDER BY t.created_at DESC`
  );

  if (transactions.rows.length === 0) return [];

  const ids = transactions.rows.map(t => t.id);
  const details = await query(
    `SELECT td.*, p.name as product_name
     FROM transaction_details td
     LEFT JOIN products p ON td.product_id = p.id
     WHERE td.transaction_id = ANY($1::int[])
     ORDER BY td.id`,
    [ids]
  );

  const detailsByTrx = {};
  for (const d of details.rows) {
    if (!detailsByTrx[d.transaction_id]) detailsByTrx[d.transaction_id] = [];
    detailsByTrx[d.transaction_id].push(d);
  }

  for (const t of transactions.rows) {
    t.details = detailsByTrx[t.id] || [];
  }

  return transactions.rows;
};

const updatePayment = async (id, paymentMethodId, amountPaid, changeAmount, status) => {
  const result = await query(
    `UPDATE transactions SET payment_method_id = $1, amount_paid = $2,
     change_amount = $3, status = $4, updated_at = NOW()
     WHERE id = $5 RETURNING *`,
    [paymentMethodId, amountPaid, changeAmount, status || 'completed', id]
  );
  return result.rows[0];
};

const findById = async (id) => {
  const result = await query(
    `SELECT t.*, u.name as kasir_name, pm.name as payment_method_name
     FROM transactions t
     LEFT JOIN users u ON t.kasir_id = u.id
     LEFT JOIN payment_methods pm ON t.payment_method_id = pm.id
     WHERE t.id = $1`,
    [id]
  );
  return result.rows[0];
};

module.exports = { create, addDetail, findByKasir, findAll, updatePayment, findById };
