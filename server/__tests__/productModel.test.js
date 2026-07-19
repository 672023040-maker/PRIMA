jest.mock('../config/database', () => ({
  query: jest.fn(),
}));

const { query } = require('../config/database');
const productModel = require('../models/productModel');

describe('productModel', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('findAll', () => {
    it('should return all active products', async () => {
      const mockProducts = [
        { id: 1, name: 'Kopi Hitam', price: 15000, category_name: 'Minuman' },
        { id: 2, name: 'Nasi Goreng', price: 28000, category_name: 'Makanan' },
      ];
      query.mockResolvedValue({ rows: mockProducts });

      const result = await productModel.findAll();

      expect(query).toHaveBeenCalledWith(
        expect.stringContaining('SELECT p.id')
      );
      expect(result).toEqual(mockProducts);
    });
  });

  describe('findById', () => {
    it('should return product by id', async () => {
      const mockProduct = { id: 1, name: 'Kopi Hitam', price: 15000, category_id: 1 };
      query.mockResolvedValue({ rows: [mockProduct] });

      const result = await productModel.findById(1);

      expect(query).toHaveBeenCalledWith(
        expect.stringContaining('WHERE p.id = $1'),
        [1]
      );
      expect(result).toEqual(mockProduct);
    });

    it('should return undefined when product not found', async () => {
      query.mockResolvedValue({ rows: [] });

      const result = await productModel.findById(999);
      expect(result).toBeUndefined();
    });
  });

  describe('create', () => {
    it('should create product', async () => {
      const mockProduct = { id: 11, name: 'Teh Manis', price: 8000, category_id: 1 };
      query.mockResolvedValue({ rows: [mockProduct] });

      const result = await productModel.create(1, 'Teh Manis', 8000, 'Teh manis segar', null, 50);

      expect(query).toHaveBeenCalledWith(
        expect.stringContaining('INSERT INTO products'),
        [1, 'Teh Manis', 8000, 'Teh manis segar', null, 50]
      );
      expect(result).toEqual(mockProduct);
    });
  });

  describe('deleteProduct', () => {
    it('should soft delete product', async () => {
      query.mockResolvedValue({});

      await productModel.deleteProduct(1);

      expect(query).toHaveBeenCalledWith(
        'UPDATE products SET is_active = FALSE, updated_at = NOW() WHERE id = $1',
        [1]
      );
    });
  });
});
