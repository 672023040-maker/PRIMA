jest.mock('../config/database', () => ({
  query: jest.fn(),
}));

const request = require('supertest');
const jwt = require('jsonwebtoken');
const { query } = require('../config/database');

let app;
let kasirToken;

beforeEach(() => {
  jest.clearAllMocks();
  app = require('../app');

  kasirToken = jwt.sign(
    { id: 1, email: 'kasir@prima.com', role: 'kasir' },
    process.env.JWT_SECRET,
    { expiresIn: '1h' }
  );
});

afterAll(() => {
  jest.restoreAllMocks();
});

describe('Transaction Routes', () => {
  describe('POST /api/transactions', () => {
    it('should create transaction', async () => {
      const mockTransaction = {
        id: 1, transaction_code: 'TRX-123456-ABC', kasir_id: 1, total: 0, status: 'active'
      };
      query.mockResolvedValue({ rows: [mockTransaction] });

      const res = await request(app)
        .post('/api/transactions')
        .set('Authorization', `Bearer ${kasirToken}`)
        .send({ kasir_id: 1 });

      expect(res.status).toBe(201);
      expect(res.body.data.transaction_code).toBeDefined();
    });

    it('should return 422 when kasir_id missing', async () => {
      const res = await request(app)
        .post('/api/transactions')
        .set('Authorization', `Bearer ${kasirToken}`)
        .send({});

      expect(res.status).toBe(422);
    });
  });

  describe('GET /api/transactions', () => {
    it('should return transactions list', async () => {
      query.mockResolvedValueOnce({ rows: [{ id: 1, transaction_code: 'TRX-123', kasir_id: 1, total: 50000 }] });
      query.mockResolvedValueOnce({ rows: [] }); // details for t[0]

      const res = await request(app)
        .get('/api/transactions')
        .set('Authorization', `Bearer ${kasirToken}`);

      expect(res.status).toBe(200);
      expect(res.body.data).toBeDefined();
    });

    it('should return 500 on error', async () => {
      query.mockRejectedValue(new Error('DB error'));

      const res = await request(app)
        .get('/api/transactions')
        .set('Authorization', `Bearer ${kasirToken}`);

      expect(res.status).toBe(500);
    });
  });

  describe('POST /api/transactions/:id/complete', () => {
    it('should complete transaction', async () => {
      query.mockResolvedValueOnce({ rows: [{ id: 1, total: 50000 }] });
      query.mockResolvedValueOnce({ rows: [{ id: 1, status: 'completed', change_amount: 50000 }] });

      const res = await request(app)
        .post('/api/transactions/1/complete')
        .set('Authorization', `Bearer ${kasirToken}`)
        .send({ payment_method_id: 1, amount_paid: 100000 });

      expect(res.status).toBe(200);
      expect(res.body.message).toContain('berhasil diselesaikan');
    });

    it('should return 422 when amount less than total', async () => {
      query.mockResolvedValue({ rows: [{ id: 1, total: 50000 }] });

      const res = await request(app)
        .post('/api/transactions/1/complete')
        .set('Authorization', `Bearer ${kasirToken}`)
        .send({ payment_method_id: 1, amount_paid: 30000 });

      expect(res.status).toBe(422);
      expect(res.body.message).toContain('kurang');
    });

    it('should return 422 when payment_method_id missing', async () => {
      const res = await request(app)
        .post('/api/transactions/1/complete')
        .set('Authorization', `Bearer ${kasirToken}`)
        .send({ amount_paid: 100000 });

      expect(res.status).toBe(422);
    });
  });
});
