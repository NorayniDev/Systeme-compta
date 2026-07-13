package com.facturationpme.payments.repository;

import com.facturationpme.payments.domain.Payment;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class PaymentSpecifications {

  private PaymentSpecifications() {}

  /** Recherche libre sur la reference, le numero de facture et le nom du client. */
  public static Specification<Payment> matchingQuery(String query) {
    if (!StringUtils.hasText(query)) {
      return null;
    }
    String pattern = "%" + query.toLowerCase() + "%";
    return (root, criteriaQuery, criteriaBuilder) ->
        criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("reference")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("invoiceNumber")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("clientName")), pattern));
  }
}
