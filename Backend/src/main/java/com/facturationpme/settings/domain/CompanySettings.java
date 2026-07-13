package com.facturationpme.settings.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Ligne singleton (id fixe, voir {@code CompanySettingsService.SINGLETON_ID}) : configuration
 * globale de l'entreprise, editable uniquement par un administrateur.
 */
@Entity
@Table(name = "company_settings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySettings {

  @Id private UUID id;

  @Column(name = "company_name", nullable = false)
  private String companyName;

  @Column(nullable = false)
  private String address;

  @Column(name = "tax_id", nullable = false)
  private String taxId;

  @Column(nullable = false)
  private String currency;

  @Column(name = "default_tax_rate", nullable = false)
  private BigDecimal defaultTaxRate;

  @Column(name = "invoice_prefix", nullable = false)
  private String invoicePrefix;

  @Column(name = "quote_prefix", nullable = false)
  private String quotePrefix;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @LastModifiedBy
  @Column(name = "updated_by")
  private String updatedBy;
}
