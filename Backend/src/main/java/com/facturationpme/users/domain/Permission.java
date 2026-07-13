package com.facturationpme.users.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Permissions granulaires au format {@code ressource:action}. Doit rester synchronise avec
 * l'enumeration equivalente du frontend Angular (core/constants/roles.constant.ts::Permission) : la
 * serialisation JSON DOIT produire la chaine {@link #value} (ex: {@code "invoice:create"}), jamais
 * le nom de la constante Java.
 */
public enum Permission {
  INVOICE_CREATE("invoice:create"),
  INVOICE_READ("invoice:read"),
  INVOICE_UPDATE("invoice:update"),
  INVOICE_DELETE("invoice:delete"),
  INVOICE_VALIDATE("invoice:validate"),

  QUOTE_CREATE("quote:create"),
  QUOTE_READ("quote:read"),
  QUOTE_UPDATE("quote:update"),
  QUOTE_DELETE("quote:delete"),

  PAYMENT_CREATE("payment:create"),
  PAYMENT_READ("payment:read"),
  PAYMENT_REFUND("payment:refund"),

  CLIENT_MANAGE("client:manage"),
  SUPPLIER_MANAGE("supplier:manage"),
  PRODUCT_MANAGE("product:manage"),
  SERVICE_MANAGE("service:manage"),

  ACCOUNTING_READ("accounting:read"),
  ACCOUNTING_MANAGE("accounting:manage"),

  REPORT_READ("report:read"),
  REPORT_EXPORT("report:export"),

  USER_MANAGE("user:manage"),
  ROLE_MANAGE("role:manage"),
  SETTINGS_MANAGE("settings:manage"),
  AUDIT_READ("audit:read");

  private static final Map<String, Permission> BY_VALUE =
      Arrays.stream(values()).collect(Collectors.toMap(Permission::getValue, Function.identity()));

  private final String value;

  Permission(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static Permission fromValue(String value) {
    Permission permission = BY_VALUE.get(value);
    if (permission == null) {
      throw new IllegalArgumentException("Permission inconnue : " + value);
    }
    return permission;
  }
}
