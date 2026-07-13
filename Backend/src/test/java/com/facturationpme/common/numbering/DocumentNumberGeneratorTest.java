package com.facturationpme.common.numbering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.Year;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentNumberGeneratorTest {

  @Mock private EntityManager entityManager;
  @Mock private Query query;

  private DocumentNumberGenerator documentNumberGenerator;

  @BeforeEach
  void setUp() {
    documentNumberGenerator = new DocumentNumberGenerator(entityManager);
  }

  @Test
  void nextShouldFormatPrefixYearAndZeroPaddedSequence() {
    when(entityManager.createNativeQuery(org.mockito.ArgumentMatchers.anyString()))
        .thenReturn(query);
    when(query.setParameter(1, "QUOTE")).thenReturn(query);
    when(query.setParameter(2, Year.now().getValue())).thenReturn(query);
    when(query.getSingleResult()).thenReturn(7);

    String number = documentNumberGenerator.next(DocumentType.QUOTE, "DEV");

    assertThat(number).isEqualTo("DEV-%d-0007".formatted(Year.now().getValue()));
  }
}
