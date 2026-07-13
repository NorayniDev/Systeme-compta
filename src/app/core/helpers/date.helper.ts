/** Formate une date ISO en format lisible localisé (jj/mm/aaaa par défaut). */
export function formatDate(isoDate: string | Date, locale = 'fr-FR'): string {
  const date = typeof isoDate === 'string' ? new Date(isoDate) : isoDate;
  return new Intl.DateTimeFormat(locale, { dateStyle: 'medium' }).format(date);
}

/** Formate une date ISO avec l'heure incluse. */
export function formatDateTime(isoDate: string | Date, locale = 'fr-FR'): string {
  const date = typeof isoDate === 'string' ? new Date(isoDate) : isoDate;
  return new Intl.DateTimeFormat(locale, { dateStyle: 'medium', timeStyle: 'short' }).format(date);
}

/** Nombre de jours entre deux dates (positif si `to` est postérieure à `from`). */
export function daysBetween(from: string | Date, to: string | Date): number {
  const fromDate = typeof from === 'string' ? new Date(from) : from;
  const toDate = typeof to === 'string' ? new Date(to) : to;
  const millisecondsPerDay = 1000 * 60 * 60 * 24;
  return Math.round((toDate.getTime() - fromDate.getTime()) / millisecondsPerDay);
}

/** Indique si une date d'échéance est dépassée par rapport à aujourd'hui. */
export function isOverdue(dueDate: string | Date): boolean {
  const due = typeof dueDate === 'string' ? new Date(dueDate) : dueDate;
  return due.getTime() < Date.now();
}
