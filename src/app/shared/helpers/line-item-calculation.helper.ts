import {
  computeTaxAmount,
  computeTotalWithTax,
  roundToTwoDecimals,
} from '../../core/helpers/currency.helper';

/** Forme minimale d'une ligne (facture ou devis) requise pour le calcul des totaux. */
export interface ILineItemInput {
  quantity: number;
  unitPrice: number;
  taxRate: number;
}

export interface ILineItemTotals {
  amountExclTax: number;
  taxAmount: number;
  totalAmount: number;
}

/** Montant HT d'une ligne (quantité × prix unitaire). */
export function computeLineAmount(quantity: number, unitPrice: number): number {
  return roundToTwoDecimals(quantity * unitPrice);
}

/** Montant TTC d'une ligne. */
export function computeLineTotal(quantity: number, unitPrice: number, taxRate: number): number {
  return computeTotalWithTax(computeLineAmount(quantity, unitPrice), taxRate);
}

/**
 * Agrège les totaux HT/TVA/TTC d'un ensemble de lignes.
 * Partagé entre les modules Factures et Devis, dont les lignes ont la même forme.
 */
export function computeLineItemTotals(lines: ILineItemInput[]): ILineItemTotals {
  return lines.reduce<ILineItemTotals>(
    (totals, line) => {
      const lineAmount = computeLineAmount(line.quantity, line.unitPrice);
      return {
        amountExclTax: roundToTwoDecimals(totals.amountExclTax + lineAmount),
        taxAmount: roundToTwoDecimals(
          totals.taxAmount + computeTaxAmount(lineAmount, line.taxRate),
        ),
        totalAmount: roundToTwoDecimals(
          totals.totalAmount + computeTotalWithTax(lineAmount, line.taxRate),
        ),
      };
    },
    { amountExclTax: 0, taxAmount: 0, totalAmount: 0 },
  );
}
