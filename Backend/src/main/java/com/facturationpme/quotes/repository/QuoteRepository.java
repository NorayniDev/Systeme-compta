package com.facturationpme.quotes.repository;

import com.facturationpme.quotes.domain.Quote;
import com.facturationpme.quotes.dto.QuoteFunnelProjection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface QuoteRepository
    extends JpaRepository<Quote, UUID>, JpaSpecificationExecutor<Quote> {

  @Query(
      "select new com.facturationpme.quotes.dto.QuoteFunnelProjection("
          + "q.status, count(q), sum(q.totalAmount)) "
          + "from Quote q "
          + "group by q.status")
  List<QuoteFunnelProjection> aggregateByStatus();
}
