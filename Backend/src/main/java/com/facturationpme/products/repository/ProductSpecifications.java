package com.facturationpme.products.repository;

import com.facturationpme.products.domain.Product;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ProductSpecifications {

  private ProductSpecifications() {}

  /** Recherche libre sur nom, SKU et categorie (correspond a {@code ISearchFilter.query}). */
  public static Specification<Product> matchingQuery(String query) {
    if (!StringUtils.hasText(query)) {
      return null;
    }
    String pattern = "%" + query.toLowerCase() + "%";
    return (root, criteriaQuery, criteriaBuilder) ->
        criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), pattern));
  }
}
