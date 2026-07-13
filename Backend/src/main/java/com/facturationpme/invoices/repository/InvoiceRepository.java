package com.facturationpme.invoices.repository;

import com.facturationpme.invoices.domain.Invoice;
import com.facturationpme.invoices.dto.AgingReceivableProjection;
import com.facturationpme.invoices.dto.InvoiceMonthlyAmountProjection;
import com.facturationpme.invoices.dto.InvoiceStatusProjection;
import com.facturationpme.invoices.dto.SalesByClientProjection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository
    extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {

  @Query(
      "select new com.facturationpme.invoices.dto.SalesByClientProjection("
          + "i.clientId, i.clientName, count(i), sum(i.amountExclTax), sum(i.taxAmount), sum(i.totalAmount)) "
          + "from Invoice i "
          + "where i.status not in (com.facturationpme.invoices.domain.InvoiceStatus.DRAFT, "
          + "com.facturationpme.invoices.domain.InvoiceStatus.CANCELLED) "
          + "and (cast(:startDate as date) is null or i.issueDate >= cast(:startDate as date)) "
          + "and (cast(:endDate as date) is null or i.issueDate <= cast(:endDate as date)) "
          + "group by i.clientId, i.clientName "
          + "order by sum(i.totalAmount) desc")
  List<SalesByClientProjection> aggregateSalesByClient(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  @Query(
      "select new com.facturationpme.invoices.dto.AgingReceivableProjection("
          + "i.id, i.number, i.clientName, i.dueDate, i.totalAmount) "
          + "from Invoice i "
          + "where i.status in (com.facturationpme.invoices.domain.InvoiceStatus.SENT, "
          + "com.facturationpme.invoices.domain.InvoiceStatus.PARTIALLY_PAID) "
          + "and i.dueDate < :today "
          + "order by i.dueDate asc")
  List<AgingReceivableProjection> findOverdueInvoices(@Param("today") LocalDate today);

  @Query(
      "select coalesce(sum(i.totalAmount), 0) from Invoice i "
          + "where i.status not in (com.facturationpme.invoices.domain.InvoiceStatus.DRAFT, "
          + "com.facturationpme.invoices.domain.InvoiceStatus.CANCELLED) "
          + "and i.issueDate >= :start and i.issueDate < :end")
  BigDecimal sumRevenueBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

  long countByIssueDateGreaterThanEqualAndIssueDateLessThan(LocalDate start, LocalDate end);

  @Query(
      "select coalesce(sum(i.totalAmount), 0) from Invoice i "
          + "where i.status in (com.facturationpme.invoices.domain.InvoiceStatus.SENT, "
          + "com.facturationpme.invoices.domain.InvoiceStatus.PARTIALLY_PAID)")
  BigDecimal sumOutstandingInvoiceAmount();

  @Query(
      "select new com.facturationpme.invoices.dto.InvoiceMonthlyAmountProjection(i.issueDate, i.totalAmount) "
          + "from Invoice i "
          + "where i.status not in (com.facturationpme.invoices.domain.InvoiceStatus.DRAFT, "
          + "com.facturationpme.invoices.domain.InvoiceStatus.CANCELLED) "
          + "and i.issueDate >= :start")
  List<InvoiceMonthlyAmountProjection> findAmountsIssuedSince(@Param("start") LocalDate start);

  @Query(
      "select new com.facturationpme.invoices.dto.InvoiceStatusProjection(i.status, i.dueDate) from Invoice i")
  List<InvoiceStatusProjection> findAllStatusesAndDueDates();
}
