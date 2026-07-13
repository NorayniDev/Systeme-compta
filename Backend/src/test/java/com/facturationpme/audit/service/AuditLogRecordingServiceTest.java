package com.facturationpme.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.facturationpme.audit.domain.AuditAction;
import com.facturationpme.audit.domain.AuditLog;
import com.facturationpme.audit.event.AuditableActionEvent;
import com.facturationpme.audit.repository.AuditLogRepository;
import com.facturationpme.users.domain.User;
import com.facturationpme.users.domain.UserRole;
import com.facturationpme.users.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditLogRecordingServiceTest {

  @Mock private AuditLogRepository auditLogRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private AuditLogRecordingService auditLogRecordingService;

  @Test
  void onAuditableActionShouldResolveDisplayNameFromActorUserId() {
    UUID userId = UUID.randomUUID();
    User user =
        User.builder()
            .id(userId)
            .firstName("Awa")
            .lastName("Ndiaye")
            .role(UserRole.GESTIONNAIRE)
            .active(true)
            .build();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    AuditableActionEvent event =
        new AuditableActionEvent(AuditAction.CREATE, "Client", "ACME Senegal SARL", userId);

    auditLogRecordingService.onAuditableAction(event);

    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditLogRepository).save(captor.capture());
    AuditLog saved = captor.getValue();
    assertThat(saved.getUserName()).isEqualTo("Awa Ndiaye");
    assertThat(saved.getAction()).isEqualTo(AuditAction.CREATE);
    assertThat(saved.getEntityType()).isEqualTo("Client");
    assertThat(saved.getEntityLabel()).isEqualTo("ACME Senegal SARL");
  }

  @Test
  void onAuditableActionShouldFallBackToSystemWhenActorIdIsNull() {
    AuditableActionEvent event =
        new AuditableActionEvent(AuditAction.LOGIN, "Auth", "Connexion", null);

    auditLogRecordingService.onAuditableAction(event);

    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditLogRepository).save(captor.capture());
    assertThat(captor.getValue().getUserName()).isEqualTo("Systeme");
  }

  @Test
  void onAuditableActionShouldFallBackToSystemWhenActorUserNotFound() {
    UUID userId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.empty());
    AuditableActionEvent event =
        new AuditableActionEvent(AuditAction.DELETE, "Devis", "DEV-2026-0009", userId);

    auditLogRecordingService.onAuditableAction(event);

    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditLogRepository).save(captor.capture());
    assertThat(captor.getValue().getUserName()).isEqualTo("Systeme");
  }
}
