package com.facturationpme.suppliers.repository;

import com.facturationpme.suppliers.domain.Supplier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class SupplierSpecifications {

  private SupplierSpecifications() {}

  /**
   * Recherche libre sur nom, email, telephone et identifiant fiscal (correspond a {@code
   * ISearchFilter.query}).
   */
  public static Specification<Supplier> matchingQuery(String query) {
    if (!StringUtils.hasText(query)) {
      return null;
    }
    String pattern = "%" + query.toLowerCase() + "%";
    return (root, criteriaQuery, criteriaBuilder) ->
        criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("taxId")), pattern));
  }
}
