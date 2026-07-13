package com.facturationpme.users.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Matrice role -> permissions, source de verite cote serveur. Doit rester une copie fidele de
 * {@code ROLE_PERMISSIONS} (core/constants/roles.constant.ts) cote frontend, qui ne s'en sert qu'en
 * repli si le backend ne renvoie pas explicitement les permissions de l'utilisateur.
 */
public final class RolePermissions {

  private static final Map<UserRole, Set<Permission>> MATRIX = new EnumMap<>(UserRole.class);

  static {
    MATRIX.put(UserRole.ADMIN, EnumSet.allOf(Permission.class));

    MATRIX.put(
        UserRole.COMPTABLE,
        EnumSet.of(
            Permission.INVOICE_CREATE,
            Permission.INVOICE_READ,
            Permission.INVOICE_UPDATE,
            Permission.INVOICE_VALIDATE,
            Permission.QUOTE_READ,
            Permission.PAYMENT_READ,
            Permission.ACCOUNTING_READ,
            Permission.ACCOUNTING_MANAGE,
            Permission.REPORT_READ,
            Permission.REPORT_EXPORT));

    MATRIX.put(
        UserRole.GESTIONNAIRE,
        EnumSet.of(
            Permission.INVOICE_CREATE,
            Permission.INVOICE_READ,
            Permission.INVOICE_UPDATE,
            Permission.QUOTE_CREATE,
            Permission.QUOTE_READ,
            Permission.QUOTE_UPDATE,
            Permission.CLIENT_MANAGE,
            Permission.SUPPLIER_MANAGE,
            Permission.PRODUCT_MANAGE,
            Permission.SERVICE_MANAGE,
            Permission.REPORT_READ));

    MATRIX.put(
        UserRole.CAISSIER,
        EnumSet.of(Permission.PAYMENT_CREATE, Permission.PAYMENT_READ, Permission.INVOICE_READ));

    MATRIX.put(
        UserRole.AUDITEUR,
        EnumSet.of(
            Permission.INVOICE_READ,
            Permission.QUOTE_READ,
            Permission.PAYMENT_READ,
            Permission.ACCOUNTING_READ,
            Permission.REPORT_READ,
            Permission.AUDIT_READ));

    MATRIX.put(UserRole.UTILISATEUR, EnumSet.of(Permission.INVOICE_READ, Permission.QUOTE_READ));
  }

  private RolePermissions() {}

  public static Set<Permission> of(UserRole role) {
    return MATRIX.getOrDefault(role, Set.of());
  }
}
