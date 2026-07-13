package com.facturationpme.common.config;

import com.facturationpme.common.security.JwtPrincipal;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class JpaAuditingConfig {

  private static final String SYSTEM_AUDITOR = "system";

  /**
   * N'utilise jamais {@code Authentication.getName()} tel quel : pour un principal qui n'est ni une
   * {@code String} ni un {@code UserDetails} (notre {@link JwtPrincipal}), cette methode retombe
   * sur {@code principal.toString()} - le dump complet du record (permissions incluses), qui
   * deborde largement la colonne {@code varchar(255)} de {@code created_by}/{@code updated_by}.
   */
  @Bean
  public AuditorAware<String> securityAuditorAware() {
    return () -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null || !authentication.isAuthenticated()) {
        return Optional.of(SYSTEM_AUDITOR);
      }
      if (authentication.getPrincipal() instanceof JwtPrincipal jwtPrincipal) {
        return Optional.of(jwtPrincipal.email());
      }
      return Optional.ofNullable(authentication.getName()).or(() -> Optional.of(SYSTEM_AUDITOR));
    };
  }
}
