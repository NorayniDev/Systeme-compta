package com.facturationpme.accounting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * Ligne elementaire de debit OU de credit. Une operation metier (validation de facture,
 * encaissement, remboursement) genere plusieurs lignes partageant la meme {@code reference},
 * conformement au principe de la partie double.
 *
 * <p>Immuable : ecrite exclusivement par {@code JournalEntryRecorder} en reaction a un evenement de
 * domaine, jamais via un endpoint HTTP (voir AccountingController, entierement en lecture seule).
 */
@Entity
@Table(name = "journal_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntry {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @Column(name = "entry_date", nullable = false)
  private LocalDate date;

  @Column(nullable = false)
  private String reference;

  @Column(name = "account_code", nullable = false)
  private String accountCode;

  @Column(name = "account_name", nullable = false)
  private String accountName;

  @Column(nullable = false)
  private String label;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal debit;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal credit;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private JournalSource source;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
