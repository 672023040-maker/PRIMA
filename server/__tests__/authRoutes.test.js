jest.mock('../config/database', () => ({
  query: jest.fn(),
}));

const request = require('supertest');
const jwt = require('jsonwebtoken');
const { query } = require('../config/database');

let app;

beforeEach(() => {
  jest.clearAllMocks();
  app = require('../app');
});

afterAll(() => {
  jest.restoreAllMocks();
});

describe('Auth Routes', () => {
  describe('POST /api/login', () => {
    it('should login with valid credentials', async () => {
      const mockUser = {
        id: 1, name: 'Kasir A', email: 'kasir@prima.com',
        password: '$2b$10$jnTfkijnwgZIkqqQpA/lm.23QdZYphUvVEx5308uMc//JKSN1szOW',
        role: 'kasir'
      };
      query.mockResolvedValue({ rows: [mockUser] });

      const res = await request(app)
        .post('/api/login')
        .send({ email: 'kasir@prima.com', password: 'kasir123' });

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Login berhasil');
      expect(res.body.token).toBeDefined();
      expect(res.body.user.email).toBe('kasir@prima.com');
    });

    it('should return 422 when email/password missing', async () => {
      const res = await request(app)
        .post('/api/login')
        .send({ email: '' });

      expect(res.status).toBe(422);
      expect(res.body.message).toContain('wajib diisi');
    });

    it('should return 401 with invalid credentials', async () => {
      query.mockResolvedValue({ rows: [] });

      const res = await request(app)
        .post('/api/login')
        .send({ email: 'wrong@prima.com', password: 'wrong' });

      expect(res.status).toBe(401);
    });
  });

  describe('POST /api/logout', () => {
    it('should return 200 (logout is always successful)', async () => {
      const token = jwt.sign(
        { id: 1, email: 'kasir@prima.com', role: 'kasir' },
        process.env.JWT_SECRET,
        { expiresIn: '1h' }
      );

      const res = await request(app)
        .post('/api/logout')
        .set('Authorization', `Bearer ${token}`);

      expect(res.status).toBe(200);
      expect(res.body.message).toBe('Logout berhasil');
    });
  });
});
