package com.facturationpme.audit.repository;

import com.facturationpme.audit.domain.AuditLog;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class AuditLogSpecifications {

  private AuditLogSpecifications() {}

  /**
   * Recherche libre sur utilisateur, type d'entite, libelle et details (correspond a {@code
   * ISearchFilter.query}, memes 4 champs que {@code paginateMock} cote mock frontend).
   */
  public static Specification<AuditLog> matchingQuery(String query) {
    if (!StringUtils.hasText(query)) {
      return null;
    }
    String pattern = "%" + query.toLowerCase() + "%";
    return (root, criteriaQuery, criteriaBuilder) ->
        criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("userName")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("entityType")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("entityLabel")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("details")), pattern));
  }
}
