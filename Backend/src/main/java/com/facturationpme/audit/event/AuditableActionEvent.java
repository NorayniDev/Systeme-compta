package com.facturationpme.audit.event;

import com.facturationpme.audit.domain.AuditAction;
import java.util.UUID;

/**
 * Evenement generique publie par n'importe quel module apres une action utilisateur significative
 * (creation, modification, suppression, validation, connexion). {@code actorUserId} est capture au
 * moment de la publication (via le SecurityContext du service appelant, ou directement connu pour
 * la connexion) - jamais resolu depuis le contexte du listener, qui s'execute apres coup.
 */
public record AuditableActionEvent(
    AuditAction action, String entityType, String entityLabel, String details, UUID actorUserId) {

  public AuditableActionEvent(
      AuditAction action, String entityType, String entityLabel, UUID actorUserId) {
    this(action, entityType, entityLabel, null, actorUserId);
  }
}
