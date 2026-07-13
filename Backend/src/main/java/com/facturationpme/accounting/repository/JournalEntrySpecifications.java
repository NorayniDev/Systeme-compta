package com.facturationpme.accounting.repository;

import com.facturationpme.accounting.domain.JournalEntry;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class JournalEntrySpecifications {

  private JournalEntrySpecifications() {}

  /** Recherche libre sur la reference, le libelle et le nom du compte. */
  public static Specification<JournalEntry> matchingQuery(String query) {
    if (!StringUtils.hasText(query)) {
      return null;
    }
    String pattern = "%" + query.toLowerCase() + "%";
    return (root, criteriaQuery, criteriaBuilder) ->
        criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("reference")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("label")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("accountName")), pattern));
  }

  public static Specification<JournalEntry> dateBetween(LocalDate startDate, LocalDate endDate) {
    if (startDate == null && endDate == null) {
      return null;
    }
    return (root, criteriaQuery, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (startDate != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startDate));
      }
      if (endDate != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), endDate));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

  public static Specification<JournalEntry> accountCodeEquals(String accountCode) {
    return (root, criteriaQuery, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("accountCode"), accountCode);
  }

  public static Specification<JournalEntry> accountCodeIn(List<String> accountCodes) {
    return (root, criteriaQuery, criteriaBuilder) -> root.get("accountCode").in(accountCodes);
  }
}
