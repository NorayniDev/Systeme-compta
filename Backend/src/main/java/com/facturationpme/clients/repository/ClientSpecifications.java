package com.facturationpme.clients.repository;

import com.facturationpme.clients.domain.Client;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ClientSpecifications {

  private ClientSpecifications() {}

  /**
   * Recherche libre sur nom, email, telephone et identifiant fiscal (correspond a {@code
   * ISearchFilter.query}).
   */
  public static Specification<Client> matchingQuery(String query) {
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
