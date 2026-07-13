package com.facturationpme.payments.repository;

import com.facturationpme.payments.domain.Payment;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository
    extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

  Optional<Payment> findByGatewaySessionId(String gatewaySessionId);

  @Query(
      "select coalesce(sum(p.amount), 0) from Payment p "
          + "where p.invoiceId = :invoiceId and p.status = com.facturationpme.payments.domain.PaymentStatus.COMPLETED")
  BigDecimal sumCompletedAmountByInvoiceId(@Param("invoiceId") UUID invoiceId);

  @Query(
      "select coalesce(sum(p.amount), 0) from Payment p "
          + "where p.status = com.facturationpme.payments.domain.PaymentStatus.COMPLETED "
          + "and p.paidAt >= :start and p.paidAt < :end")
  BigDecimal sumCompletedAmountPaidBetween(
      @Param("start") Instant start, @Param("end") Instant end);

  @Query(
      "select coalesce(sum(p.amount), 0) from Payment p "
          + "where p.status = com.facturationpme.payments.domain.PaymentStatus.COMPLETED "
          + "and p.invoiceId in (select i.id from Invoice i "
          + "where i.status in (com.facturationpme.invoices.domain.InvoiceStatus.SENT, "
          + "com.facturationpme.invoices.domain.InvoiceStatus.PARTIALLY_PAID))")
  BigDecimal sumCompletedAmountForOutstandingInvoices();
}
