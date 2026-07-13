/**
 * Export CSV léger, compatible Excel (ouverture directe, encodage UTF-8 avec
 * BOM pour les accents), sans dépendance tierce. Suffisant pour l'export de
 * listes tabulaires; à remplacer par une librairie dédiée (ex: exceljs) si
 * des feuilles de calcul multi-onglets ou du formatage avancé sont requis.
 */
export function exportToCsv<T extends object>(
  filename: string,
  rows: T[],
  headers: (keyof T)[],
): void {
  const separator = ';';
  const headerLine = headers.join(separator);
  const dataLines = rows.map((row) =>
    headers
      .map((header) => {
        const value = row[header];
        const stringValue = value === null || value === undefined ? '' : String(value);
        return `"${stringValue.replace(/"/g, '""')}"`;
      })
      .join(separator),
  );

  const csvContent = [headerLine, ...dataLines].join('\r\n');
  const blob = new Blob(['﻿' + csvContent], { type: 'text/csv;charset=utf-8;' });
  downloadBlob(blob, `${filename}.csv`);
}

/**
 * Déclenche l'impression du document (utilisée pour l'export PDF côté
 * client via la boîte de dialogue "Enregistrer en PDF" du navigateur).
 */
export function exportToPdf(): void {
  window.print();
}

/** Déclenche le téléchargement d'un blob quelconque (PDF, CSV, ...) sous le nom donné. */
export function downloadBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}
