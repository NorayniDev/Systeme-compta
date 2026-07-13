package com.facturationpme.audit.service;

import com.facturationpme.audit.domain.AuditLog;
import com.facturationpme.audit.dto.AuditLogResponse;
import com.facturationpme.audit.repository.AuditLogRepository;
import com.facturationpme.audit.repository.AuditLogSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consultation exclusivement en lecture : voir {@code AuditLogRecordingService} pour l'ecriture,
 * jamais declenchee par un endpoint HTTP.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;

  public Page<AuditLogResponse> search(String query, Pageable pageable) {
    Specification<AuditLog> specification =
        Specification.where(AuditLogSpecifications.matchingQuery(query));
    return auditLogRepository.findAll(specification, pageable).map(AuditLogResponse::from);
  }
}
