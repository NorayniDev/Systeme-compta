package com.facturationpme.common.dto;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/**
 * Forme de pagination exposee a l'API, alignee au caractere pres sur l'interface {@code IPage<T>}
 * du frontend Angular (core/models/pagination.model.ts). Ne jamais renvoyer un {@link Page} natif
 * de Spring Data directement : il expose des champs internes (pageable, sort, empty,
 * numberOfElements) qui ne font pas partie du contrat.
 */
public record PageResponse<T>(
    List<T> content,
    long totalElements,
    int totalPages,
    int number,
    int size,
    boolean first,
    boolean last) {

  public static <T> PageResponse<T> of(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.getNumber(),
        page.getSize(),
        page.isFirst(),
        page.isLast());
  }

  public static <T, R> PageResponse<R> of(Page<T> page, Function<T, R> mapper) {
    return new PageResponse<>(
        page.getContent().stream().map(mapper).toList(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.getNumber(),
        page.getSize(),
        page.isFirst(),
        page.isLast());
  }
}
