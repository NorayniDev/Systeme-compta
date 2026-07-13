package com.facturationpme.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.facturationpme.common.security.JwtPrincipal;
import com.facturationpme.users.domain.UserRole;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Reproduit un bug reel trouve en test manuel : {@code Authentication.getName()} sur un principal
 * {@link JwtPrincipal} (ni {@code String} ni {@code UserDetails}) retombe sur son {@code
 * toString()} complet, qui deborde la colonne {@code varchar(255)} de created_by/updated_by des
 * qu'un utilisateur a plusieurs permissions.
 */
class JpaAuditingConfigTest {

  private final AuditorAware<String> auditorAware = new JpaAuditingConfig().securityAuditorAware();

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldReturnSystemWhenNoAuthentication() {
    assertThat(auditorAware.getCurrentAuditor()).contains("system");
  }

  @Test
  void shouldReturnEmailFromJwtPrincipalNotItsToString() {
    JwtPrincipal principal =
        new JwtPrincipal(UUID.randomUUID(), "admin@facturation-pme.sn", UserRole.ADMIN, Set.of());
    var authentication =
        new UsernamePasswordAuthenticationToken(principal, null, java.util.List.of());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    assertThat(auditorAware.getCurrentAuditor()).contains("admin@facturation-pme.sn");
  }
}
