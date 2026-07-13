package com.facturationpme.accounting.service;

import com.facturationpme.accounting.domain.Account;
import com.facturationpme.accounting.domain.ChartOfAccounts;
import com.facturationpme.accounting.domain.JournalEntry;
import com.facturationpme.accounting.domain.JournalSource;
import com.facturationpme.accounting.repository.JournalEntryRepository;
import com.facturationpme.invoices.event.InvoiceValidatedEvent;
import com.facturationpme.payments.domain.PaymentMethod;
import com.facturationpme.payments.event.PaymentReceivedEvent;
import com.facturationpme.payments.event.PaymentRefundedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Seul point d'ecriture des ecritures comptables - jamais un controleur (voir AccountingController,
 * integralement en lecture seule). Ecoute apres commit uniquement ({@link
 * TransactionPhase#AFTER_COMMIT}) : si la transaction d'origine (validation de facture,
 * encaissement) echoue et s'annule, aucune ecriture fantome n'est produite.
 *
 * <p>Chaque methode d'ecoute est en {@link Propagation#REQUIRES_NEW} : au moment ou Spring invoque
 * un listener AFTER_COMMIT, les ressources de la transaction d'origine (deja physiquement validee)
 * sont encore liees au thread et ne sont debindees qu'apres. Sans REQUIRES_NEW, l'appel au
 * repository "participerait" a cette transaction fantome au lieu d'en ouvrir une nouvelle : les
 * ecritures seraient silencieusement perdues (persist() en memoire, jamais flush ni commit, aucune
 * exception levee).
 */
@Service
@RequiredArgsConstructor
public class JournalEntryRecordingService {

  private final JournalEntryRepository journalEntryRepository;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onInvoiceValidated(InvoiceValidatedEvent event) {
    String label = "Facture " + event.invoiceNumber() + " - " + event.clientName();

    List<JournalEntry> entries =
        new ArrayList<>(
            List.of(
                entry(
                    event.issueDate(),
                    event.invoiceNumber(),
                    ChartOfAccounts.CLIENT,
                    label,
                    event.totalAmount(),
                    BigDecimal.ZERO,
                    JournalSource.INVOICE),
                entry(
                    event.issueDate(),
                    event.invoiceNumber(),
                    ChartOfAccounts.SALES,
                    label,
                    BigDecimal.ZERO,
                    event.amountExclTax(),
                    JournalSource.INVOICE)));

    if (event.taxAmount().compareTo(BigDecimal.ZERO) > 0) {
      entries.add(
          entry(
              event.issueDate(),
              event.invoiceNumber(),
              ChartOfAccounts.VAT_COLLECTED,
              label,
              BigDecimal.ZERO,
              event.taxAmount(),
              JournalSource.INVOICE));
    }

    journalEntryRepository.saveAll(entries);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onPaymentReceived(PaymentReceivedEvent event) {
    Account treasury = treasuryAccountFor(event.method());
    String label = "Encaissement " + event.reference() + " - " + event.clientName();
    LocalDate date = toLocalDate(event.paidAt());

    journalEntryRepository.saveAll(
        List.of(
            entry(
                date,
                event.reference(),
                treasury,
                label,
                event.amount(),
                BigDecimal.ZERO,
                JournalSource.PAYMENT),
            entry(
                date,
                event.reference(),
                ChartOfAccounts.CLIENT,
                label,
                BigDecimal.ZERO,
                event.amount(),
                JournalSource.PAYMENT)));
  }

  /**
   * Ecriture inverse de l'encaissement d'origine : restaure la creance client, sort la tresorerie.
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onPaymentRefunded(PaymentRefundedEvent event) {
    Account treasury = treasuryAccountFor(event.method());
    String label = "Remboursement " + event.reference() + " - " + event.clientName();
    LocalDate date = toLocalDate(event.refundedAt());

    journalEntryRepository.saveAll(
        List.of(
            entry(
                date,
                event.reference(),
                ChartOfAccounts.CLIENT,
                label,
                event.amount(),
                BigDecimal.ZERO,
                JournalSource.PAYMENT),
            entry(
                date,
                event.reference(),
                treasury,
                label,
                BigDecimal.ZERO,
                event.amount(),
                JournalSource.PAYMENT)));
  }

  private static Account treasuryAccountFor(PaymentMethod method) {
    return method == PaymentMethod.CASH ? ChartOfAccounts.CASH : ChartOfAccounts.BANK;
  }

  private static LocalDate toLocalDate(Instant instant) {
    return instant.atZone(ZoneOffset.UTC).toLocalDate();
  }

  private JournalEntry entry(
      LocalDate date,
      String reference,
      Account account,
      String label,
      BigDecimal debit,
      BigDecimal credit,
      JournalSource source) {
    return JournalEntry.builder()
        .date(date)
        .reference(reference)
        .accountCode(account.code())
        .accountName(account.name())
        .label(label)
        .debit(debit)
        .credit(credit)
        .source(source)
        .createdAt(Instant.now())
        .build();
  }
}
