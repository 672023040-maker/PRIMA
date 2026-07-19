jest.mock('../config/database', () => ({
  query: jest.fn(),
}));

const { query } = require('../config/database');
const transactionModel = require('../models/transactionModel');

describe('transactionModel', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('create', () => {
    it('should create transaction with code', async () => {
      const mockTransaction = {
        id: 1, transaction_code: 'TRX-123456-ABC', kasir_id: 1, total: 0, status: 'active'
      };
      query.mockResolvedValue({ rows: [mockTransaction] });

      const result = await transactionModel.create(1, 'TRX-123456-ABC');

      expect(query).toHaveBeenCalledWith(
        expect.stringContaining('INSERT INTO transactions'),
        ['TRX-123456-ABC', 1]
      );
      expect(result).toEqual(mockTransaction);
    });
  });

  describe('addDetail', () => {
    it('should add detail and update transaction total', async () => {
      const mockDetail = { id: 1, transaction_id: 1, product_id: 1, quantity: 2, price: 15000, subtotal: 30000 };
      query.mockResolvedValue({ rows: [mockDetail] });

      const result = await transactionModel.addDetail(1, 1, 2, 15000);

      expect(query).toHaveBeenCalledWith(
        expect.stringContaining('INSERT INTO transaction_details'),
        [1, 1, 2, 15000, 30000]
      );
      expect(result).toEqual(mockDetail);
    });
  });

  describe('findById', () => {
    it('should return transaction', async () => {
      const mockTransaction = { id: 1, transaction_code: 'TRX-123', total: 50000 };
      query.mockResolvedValue({ rows: [mockTransaction] });

      const result = await transactionModel.findById(1);

      expect(result).toEqual(mockTransaction);
    });

    it('should return undefined when not found', async () => {
      query.mockResolvedValue({ rows: [] });

      const result = await transactionModel.findById(999);
      expect(result).toBeUndefined();
    });
  });

  describe('updatePayment', () => {
    it('should update payment info', async () => {
      const mockTransaction = { id: 1, payment_method_id: 1, amount_paid: 100000, change_amount: 50000, status: 'completed' };
      query.mockResolvedValue({ rows: [mockTransaction] });

      const result = await transactionModel.updatePayment(1, 1, 100000, 50000, 'completed');

      expect(query).toHaveBeenCalledWith(
        expect.stringContaining('UPDATE transactions SET payment_method_id'),
        [1, 100000, 50000, 'completed', 1]
      );
      expect(result).toEqual(mockTransaction);
    });
  });

  describe('findByKasir', () => {
    it('should return transactions for specific kasir', async () => {
      const mockTransactions = [{ id: 1, transaction_code: 'TRX-123', kasir_id: 1 }];
      query.mockResolvedValueOnce({ rows: mockTransactions });
      query.mockResolvedValueOnce({ rows: [] }); // details

      const result = await transactionModel.findByKasir(1);

      expect(result).toHaveLength(1);
      expect(result[0].details).toEqual([]);
    });
  });
});
