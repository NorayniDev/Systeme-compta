package com.facturationpme.accounting.repository;

import com.facturationpme.accounting.domain.JournalEntry;
import com.facturationpme.accounting.dto.TrialBalanceProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JournalEntryRepository
    extends JpaRepository<JournalEntry, UUID>, JpaSpecificationExecutor<JournalEntry> {

  @Query(
      "select new com.facturationpme.accounting.dto.TrialBalanceProjection("
          + "je.accountCode, je.accountName, sum(je.debit), sum(je.credit)) "
          + "from JournalEntry je "
          + "where (cast(:startDate as date) is null or je.date >= cast(:startDate as date)) "
          + "and (cast(:endDate as date) is null or je.date <= cast(:endDate as date)) "
          + "group by je.accountCode, je.accountName "
          + "order by je.accountCode")
  List<TrialBalanceProjection> aggregateByAccount(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
