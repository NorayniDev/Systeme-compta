package com.facturationpme.audit.service;

import com.facturationpme.audit.domain.AuditLog;
import com.facturationpme.audit.event.AuditableActionEvent;
import com.facturationpme.audit.repository.AuditLogRepository;
import com.facturationpme.users.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Seul point d'ecriture de la piste d'audit - jamais un controleur (voir AuditLogController,
 * integralement en lecture seule). Ecoute apres commit uniquement : si l'action d'origine echoue et
 * s'annule, aucune ligne d'audit fantome n'est produite.
 *
 * <p>{@code REQUIRES_NEW} est obligatoire ici (voir la lecon tiree d'Accounting/{@code
 * JournalEntryRecordingService}) : au moment ou Spring invoque un listener AFTER_COMMIT, les
 * ressources de la transaction d'origine sont encore liees au thread et ne sont debindees qu'apres
 * - sans une transaction explicitement nouvelle, l'ecriture "participerait" a cette transaction
 * fantome et serait silencieusement perdue (persist() en memoire, jamais flush ni commit, aucune
 * exception levee).
 */
@Service
@RequiredArgsConstructor
public class AuditLogRecordingService {

  private static final String SYSTEM_ACTOR = "Systeme";

  private final AuditLogRepository auditLogRepository;
  private final UserRepository userRepository;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onAuditableAction(AuditableActionEvent event) {
    AuditLog log =
        AuditLog.builder()
            .occurredAt(Instant.now())
            .userName(resolveUserName(event.actorUserId()))
            .action(event.action())
            .entityType(event.entityType())
            .entityLabel(event.entityLabel())
            .details(event.details())
            .build();
    auditLogRepository.save(log);
  }

  private String resolveUserName(UUID userId) {
    if (userId == null) {
      return SYSTEM_ACTOR;
    }
    return userRepository
        .findById(userId)
        .map(user -> user.getFirstName() + " " + user.getLastName())
        .orElse(SYSTEM_ACTOR);
  }
}
