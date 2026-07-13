package com.facturationpme.quotes.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Agregat racine : les lignes ({@link QuoteLine}) et les montants derives
 * (amountExclTax/taxAmount/totalAmount) n'existent qu'a travers ce document et sont toujours
 * recalcules cote serveur, jamais confies au client.
 */
@Entity
@Table(name = "quotes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quote {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @Column(nullable = false, unique = true)
  private String number;

  @Column(name = "client_id", nullable = false)
  private UUID clientId;

  @Column(name = "client_name", nullable = false)
  private String clientName;

  @Column(name = "issue_date", nullable = false)
  private LocalDate issueDate;

  @Column(name = "valid_until", nullable = false)
  private LocalDate validUntil;

  /**
   * Ordre gere explicitement via {@code QuoteLine.lineOrder} (pas {@code @OrderColumn}) : sur une
   * collection bidirectionnelle {@code mappedBy}, Hibernate ne peuple pas la colonne d'ordre au
   * moment de l'INSERT initial, ce qui viole la contrainte NOT NULL en base.
   */
  @OneToMany(
      mappedBy = "quote",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  @OrderBy("lineOrder ASC")
  @Builder.Default
  private List<QuoteLine> lines = new ArrayList<>();

  @Column(name = "amount_excl_tax", nullable = false, precision = 14, scale = 2)
  private BigDecimal amountExclTax;

  @Column(name = "tax_amount", nullable = false, precision = 14, scale = 2)
  private BigDecimal taxAmount;

  @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
  private BigDecimal totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private QuoteStatus status;

  private String notes;

  @Column(name = "converted_invoice_id")
  private UUID convertedInvoiceId;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @CreatedBy
  @Column(name = "created_by", updatable = false)
  private String createdBy;

  @LastModifiedBy
  @Column(name = "updated_by")
  private String updatedBy;
}
