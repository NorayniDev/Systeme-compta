package com.facturationpme.common.security;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Acces centralise a l'identite de l'utilisateur authentifie courant, hors JPA auditing. */
public final class SecurityUtils {

  private SecurityUtils() {}

  /** Retourne {@code null} si non authentifie (ex : action systeme, endpoint public). */
  public static UUID currentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
      return principal.userId();
    }
    return null;
  }
}
