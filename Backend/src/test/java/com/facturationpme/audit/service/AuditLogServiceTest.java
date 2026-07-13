package com.facturationpme.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.facturationpme.audit.domain.AuditAction;
import com.facturationpme.audit.domain.AuditLog;
import com.facturationpme.audit.repository.AuditLogRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

  @Mock private AuditLogRepository auditLogRepository;

  @InjectMocks private AuditLogService auditLogService;

  @Test
  void searchShouldMapRepositoryPageToResponsePage() {
    Pageable pageable = PageRequest.of(0, 10);
    AuditLog log =
        AuditLog.builder()
            .id(java.util.UUID.randomUUID())
            .occurredAt(Instant.now())
            .userName("Admin Demo")
            .action(AuditAction.CREATE)
            .entityType("Facture")
            .entityLabel("FAC-2026-0001")
            .build();
    when(auditLogRepository.findAll(ArgumentMatchers.<Specification<AuditLog>>any(), eq(pageable)))
        .thenReturn(new PageImpl<>(List.of(log)));

    Page<com.facturationpme.audit.dto.AuditLogResponse> result =
        auditLogService.search("facture", pageable);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).entityLabel()).isEqualTo("FAC-2026-0001");
  }
}
