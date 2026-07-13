package com.facturationpme.pdf.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilitaires de formatage appeles directement depuis les gabarits Thymeleaf (syntaxe {@code
 * T(com.facturationpme.pdf.util.PdfFormatUtils).xxx(...)}), pour garder les services d'assemblage
 * de donnees ({@code InvoicePdfService}, etc.) concentres sur la recuperation des entites.
 */
public final class PdfFormatUtils {

  private static final DateTimeFormatter LONG_DATE_FR =
      DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);

  private PdfFormatUtils() {}

  /** Ex : "1 062 000,00 XOF" - espace comme separateur de milliers, 2 decimales toujours. */
  public static String formatAmount(BigDecimal amount, String currency) {
    if (amount == null) {
      return "";
    }
    NumberFormat format = NumberFormat.getNumberInstance(Locale.FRANCE);
    format.setMinimumFractionDigits(2);
    format.setMaximumFractionDigits(2);
    String formatted = format.format(amount);
    return currency == null || currency.isBlank() ? formatted : formatted + " " + currency;
  }

  /** Ex : "13 juillet 2026". */
  public static String formatDate(LocalDate date) {
    return date == null ? "" : date.format(LONG_DATE_FR);
  }

  public static String formatDateTime(Instant instant) {
    return instant == null ? "" : formatDate(instant.atZone(ZoneOffset.UTC).toLocalDate());
  }
}
