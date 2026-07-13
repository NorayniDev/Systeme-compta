import { Pipe, PipeTransform } from '@angular/core';
import { formatCurrency } from '../../core/helpers/currency.helper';
import { APP_CONSTANTS } from '../../core/constants/app.constant';

/**
 * Formate un montant selon la devise de l'application (XOF par défaut).
 * Usage: `{{ invoice.totalAmount | currencyXof }}`
 */
@Pipe({
  name: 'currencyXof',
  standalone: true,
})
export class CurrencyXofPipe implements PipeTransform {
  transform(
    value: number | null | undefined,
    currency: string = APP_CONSTANTS.DEFAULT_CURRENCY,
  ): string {
    if (value === null || value === undefined) {
      return '—';
    }
    return formatCurrency(value, currency);
  }
}
