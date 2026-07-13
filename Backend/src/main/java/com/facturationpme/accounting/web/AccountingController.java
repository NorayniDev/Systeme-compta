package com.facturationpme.accounting.web;

import com.facturationpme.accounting.dto.AccountResponse;
import com.facturationpme.accounting.dto.JournalEntryResponse;
import com.facturationpme.accounting.dto.TrialBalanceLineResponse;
import com.facturationpme.accounting.service.AccountingService;
import com.facturationpme.common.dto.PageResponse;
import com.facturationpme.common.util.PageableFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Integralement en lecture seule : voir {@code JournalEntryRecordingService} pour l'ecriture. */
@RestController
@RequestMapping("/accounting")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('accounting:read')")
public class AccountingController {

  private final AccountingService accountingService;

  @GetMapping("/journal")
  public PageResponse<JournalEntryResponse> journal(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(required = false) String query,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate) {
    Pageable pageable = PageableFactory.of(page, size, sort, direction);
    return PageResponse.of(accountingService.getJournal(query, startDate, endDate, pageable));
  }

  @GetMapping("/journal/accounts")
  public List<AccountResponse> accounts() {
    return accountingService.getAccounts();
  }

  @GetMapping("/ledger/{accountCode}")
  public PageResponse<JournalEntryResponse> ledger(
      @PathVariable String accountCode,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate) {
    Pageable pageable = PageableFactory.of(page, size, sort, direction);
    return PageResponse.of(accountingService.getLedger(accountCode, startDate, endDate, pageable));
  }

  @GetMapping("/trial-balance")
  public List<TrialBalanceLineResponse> trialBalance(
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate) {
    return accountingService.getTrialBalance(startDate, endDate);
  }

  @GetMapping("/cashbook")
  public PageResponse<JournalEntryResponse> cashbook(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate) {
    Pageable pageable = PageableFactory.of(page, size, sort, direction);
    return PageResponse.of(accountingService.getCashbook(startDate, endDate, pageable));
  }
}
