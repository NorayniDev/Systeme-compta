package com.facturationpme.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

/**
 * Construit un {@link Pageable} a partir des parametres de requete envoyes par le frontend ({@code
 * ISearchFilter} : page, size, sort, direction), qui ne suivent pas le format {@code
 * sort=propriete,direction} attendu par le resolveur Spring Data par defaut.
 */
public final class PageableFactory {

  private PageableFactory() {}

  public static Pageable of(int page, int size, String sort, String direction) {
    if (!StringUtils.hasText(sort)) {
      return PageRequest.of(page, size);
    }
    Sort.Direction sortDirection =
        "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
    return PageRequest.of(page, size, Sort.by(sortDirection, sort));
  }
}
