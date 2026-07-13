import { APP_CONSTANTS } from '../constants/app.constant';

/**
 * Formate un montant numérique en devise locale (XOF par défaut), sans décimales
 * inutiles pour les montants entiers — usage courant en comptabilité PME.
 */
export function formatCurrency(
  amount: number,
  currency: string = APP_CONSTANTS.DEFAULT_CURRENCY,
  locale = 'fr-FR',
): string {
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
    minimumFractionDigits: currency === 'XOF' ? 0 : 2,
    maximumFractionDigits: currency === 'XOF' ? 0 : 2,
  }).format(amount);
}

/** Calcule le montant TTC à partir d'un montant HT et d'un taux de TVA (en %). */
export function computeTotalWithTax(amountExclTax: number, taxRatePercent: number): number {
  return roundToTwoDecimals(amountExclTax * (1 + taxRatePercent / 100));
}

/** Calcule le montant de la TVA à partir d'un montant HT et d'un taux (en %). */
export function computeTaxAmount(amountExclTax: number, taxRatePercent: number): number {
  return roundToTwoDecimals(amountExclTax * (taxRatePercent / 100));
}

export function roundToTwoDecimals(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100;
}
