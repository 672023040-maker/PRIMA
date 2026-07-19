jest.mock('../config/database', () => ({
  query: jest.fn(),
}));

const { query } = require('../config/database');
const userModel = require('../models/userModel');

describe('userModel', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('findByEmail', () => {
    it('should return user when found', async () => {
      const mockUser = { id: 1, name: 'Kasir A', email: 'kasir@prima.com', role: 'kasir' };
      query.mockResolvedValue({ rows: [mockUser] });

      const result = await userModel.findByEmail('kasir@prima.com');

      expect(query).toHaveBeenCalledWith('SELECT * FROM users WHERE email = $1', ['kasir@prima.com']);
      expect(result).toEqual(mockUser);
    });

    it('should return undefined when user not found', async () => {
      query.mockResolvedValue({ rows: [] });

      const result = await userModel.findByEmail('notfound@prima.com');
      expect(result).toBeUndefined();
    });
  });

  describe('findById', () => {
    it('should return user by id', async () => {
      const mockUser = { id: 1, name: 'Kasir A', email: 'kasir@prima.com', role: 'kasir' };
      query.mockResolvedValue({ rows: [mockUser] });

      const result = await userModel.findById(1);

      expect(query).toHaveBeenCalledWith(
        expect.stringContaining('SELECT id, name, email, role'),
        [1]
      );
      expect(result).toEqual(mockUser);
    });
  });

  describe('create', () => {
    it('should create user with hashed password', async () => {
      const mockUser = { id: 4, name: 'New User', email: 'new@prima.com', role: 'kasir' };
      query.mockResolvedValue({ rows: [mockUser] });

      const result = await userModel.create('New User', 'new@prima.com', 'password123', 'kasir');

      expect(query).toHaveBeenCalledWith(
        expect.stringContaining('INSERT INTO users'),
        ['New User', 'new@prima.com', expect.any(String), 'kasir']
      );
      expect(result).toEqual(mockUser);
    });
  });

  describe('comparePassword', () => {
    it('should return true for matching password', async () => {
      const result = await userModel.comparePassword('kasir123', '$2b$10$jnTfkijnwgZIkqqQpA/lm.23QdZYphUvVEx5308uMc//JKSN1szOW');
      expect(result).toBe(true);
    });

    it('should return false for wrong password', async () => {
      const result = await userModel.comparePassword('wrong', '$2b$10$jnTfkijnwgZIkqqQpA/lm.23QdZYphUvVEx5308uMc//JKSN1szOW');
      expect(result).toBe(false);
    });
  });
});
