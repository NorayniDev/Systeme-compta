package com.facturationpme.services.repository;

import com.facturationpme.services.domain.ServiceItem;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ServiceItemSpecifications {

  private ServiceItemSpecifications() {}

  /** Recherche libre sur nom, code et categorie (correspond a {@code ISearchFilter.query}). */
  public static Specification<ServiceItem> matchingQuery(String query) {
    if (!StringUtils.hasText(query)) {
      return null;
    }
    String pattern = "%" + query.toLowerCase() + "%";
    return (root, criteriaQuery, criteriaBuilder) ->
        criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), pattern));
  }
}
