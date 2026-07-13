package com.facturationpme.pdf.util;

import com.facturationpme.invoices.domain.InvoiceStatus;
import com.facturationpme.payments.domain.PaymentMethod;
import com.facturationpme.payments.domain.PaymentStatus;
import com.facturationpme.quotes.domain.QuoteStatus;

/**
 * Libelles francais des enums metier, appeles depuis les gabarits Thymeleaf - mêmes libelles que
 * {@code public/i18n/fr.json} cote frontend, pour rester coherent avec le reste de l'application.
 */
public final class PdfLabels {

  private PdfLabels() {}

  public static String invoiceStatus(InvoiceStatus status) {
    return switch (status) {
      case DRAFT -> "Brouillon";
      case SENT -> "Envoyee";
      case PAID -> "Payee";
      case PARTIALLY_PAID -> "Partiellement payee";
      case OVERDUE -> "En retard";
      case CANCELLED -> "Annulee";
    };
  }

  public static String quoteStatus(QuoteStatus status) {
    return switch (status) {
      case DRAFT -> "Brouillon";
      case SENT -> "Envoye";
      case ACCEPTED -> "Accepte";
      case REJECTED -> "Refuse";
      case EXPIRED -> "Expire";
      case CONVERTED -> "Converti en facture";
    };
  }

  public static String paymentStatus(PaymentStatus status) {
    return switch (status) {
      case PENDING -> "En attente";
      case COMPLETED -> "Complete";
      case FAILED -> "Echoue";
      case REFUNDED -> "Rembourse";
    };
  }

  public static String paymentMethod(PaymentMethod method) {
    return switch (method) {
      case CASH -> "Especes";
      case BANK_TRANSFER -> "Virement bancaire";
      case MOBILE_MONEY -> "Mobile Money";
      case CHECK -> "Cheque";
      case CARD -> "Carte bancaire";
    };
  }
}
