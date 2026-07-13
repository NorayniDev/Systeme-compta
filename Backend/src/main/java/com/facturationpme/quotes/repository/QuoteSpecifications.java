package com.facturationpme.quotes.repository;

import com.facturationpme.quotes.domain.Quote;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class QuoteSpecifications {

  private QuoteSpecifications() {}

  /**
   * Recherche libre sur le numero et le nom du client (correspond a {@code ISearchFilter.query}).
   */
  public static Specification<Quote> matchingQuery(String query) {
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
