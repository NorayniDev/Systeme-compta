package com.facturationpme.users.repository;

import com.facturationpme.users.domain.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class UserSpecifications {

  private UserSpecifications() {}

  /** Recherche libre sur prenom, nom et email (correspond a {@code ISearchFilter.query}). */
  public static Specification<User> matchingQuery(String query) {
    if (!StringUtils.hasText(query)) {
      return null;
    }
    String pattern = "%" + query.toLowerCase() + "%";
    return (root, criteriaQuery, criteriaBuilder) ->
        criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern));
  }
}
