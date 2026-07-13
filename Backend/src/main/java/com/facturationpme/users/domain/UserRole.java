package com.facturationpme.users.domain;

/**
 * Roles applicatifs pris en charge par le controle d'acces RBAC. Doit rester synchronise avec
 * l'enumeration equivalente du frontend Angular (core/constants/roles.constant.ts::UserRole).
 */
public enum UserRole {
  ADMIN,
  COMPTABLE,
  GESTIONNAIRE,
  CAISSIER,
  AUDITEUR,
  UTILISATEUR
}
