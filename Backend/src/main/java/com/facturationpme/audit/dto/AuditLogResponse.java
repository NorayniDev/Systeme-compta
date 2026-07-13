package com.facturationpme.audit.dto;

import com.facturationpme.audit.domain.AuditAction;
import com.facturationpme.audit.domain.AuditLog;
import java.time.Instant;

/** Reponse alignee sur {@code IAuditLog} (features/audit/models/audit-log.model.ts). */
public record AuditLogResponse(
    String id,
    Instant timestamp,
    String userName,
    AuditAction action,
    String entityType,
    String entityLabel,
    String details) {

  public static AuditLogResponse from(AuditLog auditLog) {
    return new AuditLogResponse(
        auditLog.getId().toString(),
        auditLog.getOccurredAt(),
        auditLog.getUserName(),
        auditLog.getAction(),
        auditLog.getEntityType(),
        auditLog.getEntityLabel(),
        auditLog.getDetails());
  }
}
