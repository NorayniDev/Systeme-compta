package com.facturationpme.audit.web;

import com.facturationpme.audit.dto.AuditLogResponse;
import com.facturationpme.audit.service.AuditLogService;
import com.facturationpme.common.dto.PageResponse;
import com.facturationpme.common.util.PageableFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('audit:read')")
public class AuditLogController {

  private final AuditLogService auditLogService;

  @GetMapping
  public PageResponse<AuditLogResponse> search(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(required = false) String query) {
    Pageable pageable = PageableFactory.of(page, size, toEntityProperty(sort), direction);
    return PageResponse.of(auditLogService.search(query, pageable));
  }

  /**
   * {@code IAuditLog.timestamp} (contrat frontend) correspond a {@code AuditLog.occurredAt} cote
   * entite - seule colonne triable de la page Audit ({@code mat-sort-header}), donc le seul champ
   * ou ce decalage de nom peut effectivement se produire aujourd'hui.
   */
  private static String toEntityProperty(String sort) {
    return "timestamp".equals(sort) ? "occurredAt" : sort;
  }
}
