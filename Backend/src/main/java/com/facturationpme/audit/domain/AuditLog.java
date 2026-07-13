package com.facturationpme.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * Ligne immuable de la piste d'audit - ecrite exclusivement par {@code AuditLogRecordingService} en
 * reaction a {@code AuditableActionEvent}, jamais via un endpoint HTTP (voir AuditLogController,
 * entierement en lecture seule).
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "user_name", nullable = false)
  private String userName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AuditAction action;

  @Column(name = "entity_type", nullable = false)
  private String entityType;

  @Column(name = "entity_label", nullable = false)
  private String entityLabel;

  @Column private String details;
}
