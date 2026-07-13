package com.facturationpme.payments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
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
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Registre de paiements, immuable une fois COMPLETED (correction uniquement via remboursement,
 * jamais par edition/suppression - voir PaymentController, qui n'expose ni PUT ni DELETE,
 * contrairement aux autres modules CRUD).
 *
 * <p>{@code gatewayProvider}/{@code gatewaySessionId} sont des details internes de correlation avec
 * un paiement en ligne (Wave/Orange Money) - jamais exposes dans {@code PaymentResponse}, le
 * frontend ne connait que {@code IPayment}.
 */
@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @Column(nullable = false, unique = true)
  private String reference;

  @Column(name = "invoice_id", nullable = false)
  private UUID invoiceId;

  @Column(name = "invoice_number", nullable = false)
  private String invoiceNumber;

  @Column(name = "client_id", nullable = false)
  private UUID clientId;

  @Column(name = "client_name", nullable = false)
  private String clientName;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentMethod method;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  @Column(name = "paid_at", nullable = false)
  private Instant paidAt;

  private String notes;

  @Column(name = "gateway_provider")
  private String gatewayProvider;

  @Column(name = "gateway_session_id", unique = true)
  private String gatewaySessionId;

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
