import {
  computeLineAmount,
  computeLineItemTotals,
  computeLineTotal,
} from './line-item-calculation.helper';

describe('line-item-calculation.helper', () => {
  it('should compute the excl.-tax amount of a line', () => {
    expect(computeLineAmount(3, 1000)).toBe(3000);
  });

  it('should compute the incl.-tax total of a line', () => {
    expect(computeLineTotal(2, 1000, 18)).toBe(2360);
  });

  it('should aggregate totals across multiple lines', () => {
    const totals = computeLineItemTotals([
      { quantity: 1, unitPrice: 100_000, taxRate: 18 },
      { quantity: 2, unitPrice: 50_000, taxRate: 0 },
    ]);

    expect(totals.amountExclTax).toBe(200_000);
    expect(totals.taxAmount).toBe(18_000);
    expect(totals.totalAmount).toBe(218_000);
  });

  it('should return zeroed totals for an empty line list', () => {
    expect(computeLineItemTotals([])).toEqual({ amountExclTax: 0, taxAmount: 0, totalAmount: 0 });
  });
});
