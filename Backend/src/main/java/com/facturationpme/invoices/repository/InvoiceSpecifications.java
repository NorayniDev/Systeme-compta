package com.facturationpme.invoices.repository;

import com.facturationpme.invoices.domain.Invoice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class InvoiceSpecifications {

  private InvoiceSpecifications() {}

  /**
   * Recherche libre sur le numero et le nom du client (correspond a {@code ISearchFilter.query}).
   */
  public static Specification<Invoice> matchingQuery(String query) {
    if (!StringUtils.hasText(query)) {
      return null;
    }
    String pattern = "%" + query.toLowerCase() + "%";
    return (root, criteriaQuery, criteriaBuilder) ->
        criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("number")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("clientName")), pattern));
  }
}
