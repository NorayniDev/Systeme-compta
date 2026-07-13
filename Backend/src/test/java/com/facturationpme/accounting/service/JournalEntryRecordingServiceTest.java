package com.facturationpme.accounting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

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
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JournalEntryRecordingServiceTest {

  @Mock private JournalEntryRepository journalEntryRepository;
  @Captor private ArgumentCaptor<List<JournalEntry>> entriesCaptor;

  private JournalEntryRecordingService journalEntryRecordingService;

  @BeforeEach
  void setUp() {
    journalEntryRecordingService = new JournalEntryRecordingService(journalEntryRepository);
  }

  @Test
  void onInvoiceValidatedShouldRecordDoubleEntryWithVat() {
    InvoiceValidatedEvent event =
        new InvoiceValidatedEvent(
            UUID.randomUUID(),
            "FAC-2026-0099",
            "ACME Senegal SARL",
            LocalDate.of(2026, 7, 1),
            BigDecimal.valueOf(100000),
            BigDecimal.valueOf(18000),
            BigDecimal.valueOf(118000));

    journalEntryRecordingService.onInvoiceValidated(event);

    verify(journalEntryRepository).saveAll(entriesCaptor.capture());
    List<JournalEntry> entries = entriesCaptor.getValue();

    assertThat(entries).hasSize(3);
    assertBalanced(entries);
    assertThat(entries)
        .anySatisfy(
            e -> {
              assertThat(e.getAccountCode()).isEqualTo("411");
              assertThat(e.getDebit()).isEqualByComparingTo("118000");
              assertThat(e.getCredit()).isEqualByComparingTo("0");
              assertThat(e.getSource()).isEqualTo(JournalSource.INVOICE);
            });
    assertThat(entries)
        .anySatisfy(
            e -> {
              assertThat(e.getAccountCode()).isEqualTo("706");
              assertThat(e.getCredit()).isEqualByComparingTo("100000");
            });
    assertThat(entries)
        .anySatisfy(
            e -> {
              assertThat(e.getAccountCode()).isEqualTo("44571");
              assertThat(e.getCredit()).isEqualByComparingTo("18000");
            });
  }

  @Test
  void onInvoiceValidatedShouldOmitVatLineWhenTaxIsZero() {
    InvoiceValidatedEvent event =
        new InvoiceValidatedEvent(
            UUID.randomUUID(),
            "FAC-2026-0099",
            "ACME Senegal SARL",
            LocalDate.of(2026, 7, 1),
            BigDecimal.valueOf(100000),
            BigDecimal.ZERO,
            BigDecimal.valueOf(100000));

    journalEntryRecordingService.onInvoiceValidated(event);

    verify(journalEntryRepository).saveAll(entriesCaptor.capture());
    List<JournalEntry> entries = entriesCaptor.getValue();

    assertThat(entries).hasSize(2);
    assertThat(entries).noneMatch(e -> e.getAccountCode().equals("44571"));
    assertBalanced(entries);
  }

  @Test
  void onPaymentReceivedByCashShouldDebitCashAccountAndCreditClient() {
    PaymentReceivedEvent event =
        new PaymentReceivedEvent(
            UUID.randomUUID(),
            "PAY-2026-0099",
            "ACME Senegal SARL",
            Instant.parse("2026-07-01T10:00:00Z"),
            BigDecimal.valueOf(50000),
            PaymentMethod.CASH);

    journalEntryRecordingService.onPaymentReceived(event);

    verify(journalEntryRepository).saveAll(entriesCaptor.capture());
    List<JournalEntry> entries = entriesCaptor.getValue();

    assertThat(entries).hasSize(2);
    assertBalanced(entries);
    assertThat(entries)
        .anySatisfy(
            e -> {
              assertThat(e.getAccountCode()).isEqualTo("531");
              assertThat(e.getDebit()).isEqualByComparingTo("50000");
            });
    assertThat(entries)
        .anySatisfy(
            e -> {
              assertThat(e.getAccountCode()).isEqualTo("411");
              assertThat(e.getCredit()).isEqualByComparingTo("50000");
            });
  }

  @Test
  void onPaymentReceivedByBankTransferShouldUseBankAccount() {
    PaymentReceivedEvent event =
        new PaymentReceivedEvent(
            UUID.randomUUID(),
            "PAY-2026-0100",
            "ACME Senegal SARL",
            Instant.parse("2026-07-01T10:00:00Z"),
            BigDecimal.valueOf(50000),
            PaymentMethod.BANK_TRANSFER);

    journalEntryRecordingService.onPaymentReceived(event);

    verify(journalEntryRepository).saveAll(entriesCaptor.capture());

    assertThat(entriesCaptor.getValue())
        .anySatisfy(e -> assertThat(e.getAccountCode()).isEqualTo("512"));
  }

  @Test
  void onPaymentRefundedShouldReverseOriginalEntry() {
    PaymentRefundedEvent event =
        new PaymentRefundedEvent(
            UUID.randomUUID(),
            "PAY-2026-0099",
            "ACME Senegal SARL",
            Instant.parse("2026-07-02T10:00:00Z"),
            BigDecimal.valueOf(50000),
            PaymentMethod.CASH);

    journalEntryRecordingService.onPaymentRefunded(event);

    verify(journalEntryRepository).saveAll(entriesCaptor.capture());
    List<JournalEntry> entries = entriesCaptor.getValue();

    assertThat(entries).hasSize(2);
    assertBalanced(entries);
    assertThat(entries)
        .anySatisfy(
            e -> {
              assertThat(e.getAccountCode()).isEqualTo("411");
              assertThat(e.getDebit()).isEqualByComparingTo("50000");
            });
    assertThat(entries)
        .anySatisfy(
            e -> {
              assertThat(e.getAccountCode()).isEqualTo("531");
              assertThat(e.getCredit()).isEqualByComparingTo("50000");
            });
  }

  private static void assertBalanced(List<JournalEntry> entries) {
    BigDecimal totalDebit =
        entries.stream().map(JournalEntry::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalCredit =
        entries.stream().map(JournalEntry::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertThat(totalDebit).isEqualByComparingTo(totalCredit);
  }
}
