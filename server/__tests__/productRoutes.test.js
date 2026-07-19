jest.mock('../config/database', () => ({
  query: jest.fn(),
}));

const request = require('supertest');
const jwt = require('jsonwebtoken');
const { query } = require('../config/database');

let app;
let adminToken;
let kasirToken;

beforeEach(() => {
  jest.clearAllMocks();
  app = require('../app');

  adminToken = jwt.sign(
    { id: 2, email: 'admin@prima.com', role: 'admin' },
    process.env.JWT_SECRET,
    { expiresIn: '1h' }
  );

  kasirToken = jwt.sign(
    { id: 1, email: 'kasir@prima.com', role: 'kasir' },
    process.env.JWT_SECRET,
    { expiresIn: '1h' }
  );
});

afterAll(() => {
  jest.restoreAllMocks();
});

describe('Product Routes', () => {
  describe('GET /api/products', () => {
    it('should return products list', async () => {
      const mockProducts = [
        { id: 1, name: 'Kopi Hitam', price: 15000, category_name: 'Minuman' },
        { id: 2, name: 'Nasi Goreng', price: 28000, category_name: 'Makanan' },
      ];
      query.mockResolvedValue({ rows: mockProducts });

      const res = await request(app)
        .get('/api/products')
        .set('Authorization', `Bearer ${adminToken}`);

      expect(res.status).toBe(200);
      expect(res.body.data).toEqual(mockProducts);
    });

    it('should return 401 without token', async () => {
      const res = await request(app).get('/api/products');
      expect(res.status).toBe(401);
    });
  });

  describe('POST /api/products', () => {
    it('should create product as admin', async () => {
      const mockProduct = { id: 11, name: 'Teh Manis', price: 8000 };
      query.mockResolvedValue({ rows: [mockProduct] });

      const res = await request(app)
        .post('/api/products')
        .set('Authorization', `Bearer ${adminToken}`)
        .send({ name: 'Teh Manis', price: 8000, description: 'Teh manis' });

      expect(res.status).toBe(201);
      expect(res.body.data.name).toBe('Teh Manis');
    });

    it('should return 403 for kasir role', async () => {
      const res = await request(app)
        .post('/api/products')
        .set('Authorization', `Bearer ${kasirToken}`)
        .send({ name: 'Teh Manis', price: 8000 });

      expect(res.status).toBe(403);
    });

    it('should return 422 when name missing', async () => {
      const res = await request(app)
        .post('/api/products')
        .set('Authorization', `Bearer ${adminToken}`)
        .send({ price: 8000 });

      expect(res.status).toBe(422);
    });
  });
});
