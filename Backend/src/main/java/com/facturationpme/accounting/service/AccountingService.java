package com.facturationpme.accounting.service;

import com.facturationpme.accounting.domain.ChartOfAccounts;
import com.facturationpme.accounting.domain.JournalEntry;
import com.facturationpme.accounting.dto.AccountResponse;
import com.facturationpme.accounting.dto.JournalEntryResponse;
import com.facturationpme.accounting.dto.TrialBalanceLineResponse;
import com.facturationpme.accounting.dto.TrialBalanceProjection;
import com.facturationpme.accounting.mapper.JournalEntryMapper;
import com.facturationpme.accounting.repository.JournalEntryRepository;
import com.facturationpme.accounting.repository.JournalEntrySpecifications;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vue de consultation exclusivement en lecture : aucune methode d'ecriture ici, les ecritures sont
 * produites par {@link JournalEntryRecordingService} en reaction aux evenements de domaine (facture
 * validee, paiement recu ou rembourse), jamais a la demande d'un appel HTTP.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountingService {

  private static final List<String> TREASURY_ACCOUNT_CODES =
      List.of(ChartOfAccounts.CASH.code(), ChartOfAccounts.BANK.code());

  private final JournalEntryRepository journalEntryRepository;
  private final JournalEntryMapper journalEntryMapper;

  public Page<JournalEntryResponse> getJournal(
      String query, LocalDate startDate, LocalDate endDate, Pageable pageable) {
    Specification<JournalEntry> specification =
        Specification.where(JournalEntrySpecifications.matchingQuery(query))
            .and(JournalEntrySpecifications.dateBetween(startDate, endDate));
    return journalEntryRepository
        .findAll(specification, pageable)
        .map(journalEntryMapper::toResponse);
  }

  public Page<JournalEntryResponse> getLedger(
      String accountCode, LocalDate startDate, LocalDate endDate, Pageable pageable) {
    Specification<JournalEntry> specification =
        Specification.where(JournalEntrySpecifications.accountCodeEquals(accountCode))
            .and(JournalEntrySpecifications.dateBetween(startDate, endDate));
    return journalEntryRepository
        .findAll(specification, pageable)
        .map(journalEntryMapper::toResponse);
  }

  public Page<JournalEntryResponse> getCashbook(
      LocalDate startDate, LocalDate endDate, Pageable pageable) {
    Specification<JournalEntry> specification =
        Specification.where(JournalEntrySpecifications.accountCodeIn(TREASURY_ACCOUNT_CODES))
            .and(JournalEntrySpecifications.dateBetween(startDate, endDate));
    return journalEntryRepository
        .findAll(specification, pageable)
        .map(journalEntryMapper::toResponse);
  }

  public List<TrialBalanceLineResponse> getTrialBalance(LocalDate startDate, LocalDate endDate) {
    return journalEntryRepository.aggregateByAccount(startDate, endDate).stream()
        .map(AccountingService::toTrialBalanceLine)
        .toList();
  }

  public List<AccountResponse> getAccounts() {
    return ChartOfAccounts.ALL.stream()
        .map(a -> new AccountResponse(a.code(), a.name(), a.type()))
        .toList();
  }

  private static TrialBalanceLineResponse toTrialBalanceLine(TrialBalanceProjection projection) {
    BigDecimal balance = projection.totalDebit().subtract(projection.totalCredit());
    return new TrialBalanceLineResponse(
        projection.accountCode(),
        projection.accountName(),
        projection.totalDebit(),
        projection.totalCredit(),
        balance);
  }
}
