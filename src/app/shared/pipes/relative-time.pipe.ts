import { Pipe, PipeTransform } from '@angular/core';

/**
 * Affiche une date sous forme relative ("il y a 5 minutes").
 * Usage: `{{ notification.createdAt | relativeTime }}`
 */
@Pipe({
  name: 'relativeTime',
  standalone: true,
})
export class RelativeTimePipe implements PipeTransform {
  private readonly formatter = new Intl.RelativeTimeFormat('fr', { numeric: 'auto' });

  private readonly thresholds: [number, Intl.RelativeTimeFormatUnit][] = [
    [60, 'second'],
    [3600, 'minute'],
    [86_400, 'hour'],
    [2_592_000, 'day'],
    [31_536_000, 'month'],
    [Infinity, 'year'],
  ];

  private readonly divisors: Record<Intl.RelativeTimeFormatUnit, number> = {
    second: 1,
    seconds: 1,
    minute: 60,
    minutes: 60,
    hour: 3600,
    hours: 3600,
    day: 86_400,
    days: 86_400,
    week: 604_800,
    weeks: 604_800,
    month: 2_592_000,
    months: 2_592_000,
    year: 31_536_000,
    years: 31_536_000,
    quarter: 7_776_000,
    quarters: 7_776_000,
  };

  transform(value: string | Date | null | undefined): string {
    if (!value) {
      return '';
    }
    const date = typeof value === 'string' ? new Date(value) : value;
    const elapsedSeconds = (date.getTime() - Date.now()) / 1000;
    const absSeconds = Math.abs(elapsedSeconds);

    const [, unit] = this.thresholds.find(([limit]) => absSeconds < limit) ?? [Infinity, 'year'];
    const roundedValue = Math.round(elapsedSeconds / this.divisors[unit]);
    return this.formatter.format(roundedValue, unit);
  }
}
