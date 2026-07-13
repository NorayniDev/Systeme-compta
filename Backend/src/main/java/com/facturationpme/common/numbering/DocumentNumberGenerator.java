package com.facturationpme.common.numbering;

import jakarta.persistence.EntityManager;
import java.time.Year;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Genere des references sequentielles lisibles (ex: {@code DEV-2026-0001}), un compteur distinct
 * par type de document et par annee civile. L'increment passe par un upsert SQL atomique ({@code
 * INSERT ... ON CONFLICT ... DO UPDATE ... RETURNING}) pour rester correct sous acces concurrents
 * sans verrou applicatif.
 */
@Service
@RequiredArgsConstructor
public class DocumentNumberGenerator {

  private static final String INCREMENT_AND_GET_SQL =
      "INSERT INTO document_number_counters (doc_type, year, last_value) "
          + "VALUES (?1, ?2, 1) "
          + "ON CONFLICT (doc_type, year) "
          + "DO UPDATE SET last_value = document_number_counters.last_value + 1 "
          + "RETURNING last_value";

  private final EntityManager entityManager;

  @Transactional
  public String next(DocumentType type, String prefix) {
    int year = Year.now().getValue();
    Number sequence =
        (Number)
            entityManager
                .createNativeQuery(INCREMENT_AND_GET_SQL)
                .setParameter(1, type.name())
                .setParameter(2, year)
                .getSingleResult();
    return "%s-%d-%04d".formatted(prefix, year, sequence.intValue());
  }
}
