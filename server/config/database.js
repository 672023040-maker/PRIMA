const { Pool } = require('pg');
require('dotenv').config();

const {
  DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
} = process.env;

if (!DB_HOST || !DB_NAME || !DB_USER || !DB_PASSWORD) {
  console.error('Missing required database environment variables: DB_HOST, DB_NAME, DB_USER, DB_PASSWORD');
  process.exit(1);
}

const pool = new Pool({
  host: DB_HOST,
  port: parseInt(DB_PORT || '5432', 10),
  database: DB_NAME,
  user: DB_USER,
  password: DB_PASSWORD,
  connectionTimeoutMillis: 10000,
  query_timeout: 30000,
  idle_in_transaction_session_timeout: 60000,
});

pool.on('error', (err) => {
  console.error('Unexpected database pool error:', err);
});

const query = (text, params) => pool.query(text, params);

module.exports = { pool, query };
