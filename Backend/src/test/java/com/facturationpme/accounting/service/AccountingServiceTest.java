package com.facturationpme.accounting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import com.facturationpme.accounting.domain.JournalEntry;
import com.facturationpme.accounting.domain.JournalSource;
import com.facturationpme.accounting.dto.AccountResponse;
import com.facturationpme.accounting.dto.JournalEntryResponse;
import com.facturationpme.accounting.dto.TrialBalanceLineResponse;
import com.facturationpme.accounting.dto.TrialBalanceProjection;
import com.facturationpme.accounting.mapper.JournalEntryMapper;
import com.facturationpme.accounting.repository.JournalEntryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class AccountingServiceTest {

  @Mock private JournalEntryRepository journalEntryRepository;
  @Mock private JournalEntryMapper journalEntryMapper;

  @InjectMocks private AccountingService accountingService;

  private JournalEntry entry() {
    return JournalEntry.builder()
        .date(LocalDate.of(2026, 5, 1))
        .reference("FAC-2026-0001")
        .accountCode("411")
        .accountName("Clients")
        .label("Facture FAC-2026-0001")
        .debit(BigDecimal.valueOf(118000))
        .credit(BigDecimal.ZERO)
        .source(JournalSource.INVOICE)
        .build();
  }

  @Test
  void getJournalShouldMapRepositoryPageToResponsePage() {
    Pageable pageable = PageRequest.of(0, 10);
    JournalEntry entry = entry();
    Page<JournalEntry> page = new PageImpl<>(List.of(entry));
    JournalEntryResponse expected =
        new JournalEntryResponse(
            "id",
            entry.getDate(),
            entry.getReference(),
            entry.getAccountCode(),
            entry.getAccountName(),
            entry.getLabel(),
            entry.getDebit(),
            entry.getCredit(),
            entry.getSource());
    when(journalEntryRepository.findAll(
            ArgumentMatchers.<Specification<JournalEntry>>any(), eq(pageable)))
        .thenReturn(page);
    when(journalEntryMapper.toResponse(entry)).thenReturn(expected);

    Page<JournalEntryResponse> result = accountingService.getJournal("FAC", null, null, pageable);

    assertThat(result.getContent()).containsExactly(expected);
  }

  @Test
  void getLedgerShouldFilterByAccountCode() {
    Pageable pageable = PageRequest.of(0, 10);
    when(journalEntryRepository.findAll(
            ArgumentMatchers.<Specification<JournalEntry>>any(), eq(pageable)))
        .thenReturn(new PageImpl<>(List.of()));

    Page<JournalEntryResponse> result = accountingService.getLedger("411", null, null, pageable);

    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void getCashbookShouldFilterByTreasuryAccounts() {
    Pageable pageable = PageRequest.of(0, 10);
    when(journalEntryRepository.findAll(
            ArgumentMatchers.<Specification<JournalEntry>>any(), eq(pageable)))
        .thenReturn(new PageImpl<>(List.of()));

    Page<JournalEntryResponse> result = accountingService.getCashbook(null, null, pageable);

    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void getTrialBalanceShouldComputeBalanceFromDebitAndCredit() {
    when(journalEntryRepository.aggregateByAccount(isNull(), isNull()))
        .thenReturn(
            List.of(
                new TrialBalanceProjection(
                    "411", "Clients", BigDecimal.valueOf(118000), BigDecimal.valueOf(50000)),
                new TrialBalanceProjection(
                    "706",
                    "Prestations de services",
                    BigDecimal.ZERO,
                    BigDecimal.valueOf(100000))));

    List<TrialBalanceLineResponse> result = accountingService.getTrialBalance(null, null);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).balance()).isEqualByComparingTo("68000");
    assertThat(result.get(1).balance()).isEqualByComparingTo("-100000");
  }

  @Test
  void getAccountsShouldReturnFixedChartOfAccounts() {
    List<AccountResponse> accounts = accountingService.getAccounts();

    assertThat(accounts).hasSize(5);
    assertThat(accounts)
        .extracting(AccountResponse::code)
        .contains("411", "512", "531", "706", "44571");
  }
}
