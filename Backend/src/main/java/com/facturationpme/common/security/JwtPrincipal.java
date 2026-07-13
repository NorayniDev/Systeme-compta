package com.facturationpme.common.security;

import com.facturationpme.users.domain.Permission;
import com.facturationpme.users.domain.UserRole;
import java.util.Set;
import java.util.UUID;

/**
 * Principal reconstruit a partir des claims du JWT d'acces, sans requete base de donnees a chaque
 * appel (philosophie stateless du JWT). Utilise pour {@code SecurityContextHolder}, pas pour
 * representer l'entite persistee.
 */
public record JwtPrincipal(UUID userId, String email, UserRole role, Set<Permission> permissions) {}
