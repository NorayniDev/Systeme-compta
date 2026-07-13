package com.facturationpme.quotes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.facturationpme.clients.domain.Client;
import com.facturationpme.clients.domain.ClientStatus;
import com.facturationpme.clients.repository.ClientRepository;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.common.numbering.DocumentNumberGenerator;
import com.facturationpme.common.numbering.DocumentType;
import com.facturationpme.quotes.domain.Quote;
import com.facturationpme.quotes.domain.QuoteStatus;
import com.facturationpme.quotes.dto.QuoteCreateDto;
import com.facturationpme.quotes.dto.QuoteLineDto;
import com.facturationpme.quotes.dto.QuoteResponse;
import com.facturationpme.quotes.dto.QuoteUpdateDto;
import com.facturationpme.quotes.mapper.QuoteMapper;
import com.facturationpme.quotes.repository.QuoteRepository;
import com.facturationpme.quotes.service.QuoteService;
import com.facturationpme.settings.service.CompanySettingsService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

  @Mock private QuoteRepository quoteRepository;
  @Mock private ClientRepository clientRepository;
  @Mock private QuoteMapper quoteMapper;
  @Mock private DocumentNumberGenerator documentNumberGenerator;
  @Mock private CompanySettingsService companySettingsService;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private QuoteService quoteService;

  private Client client;
  private UUID clientId;
  private UUID quoteId;
  private Quote quote;

  @BeforeEach
  void setUp() {
    clientId = UUID.randomUUID();
    client =
        Client.builder()
            .id(clientId)
            .name("ACME Senegal SARL")
            .email("contact@acme.sn")
            .status(ClientStatus.ACTIVE)
            .totalInvoiced(BigDecimal.ZERO)
            .build();

    quoteId = UUID.randomUUID();
    quote =
        Quote.builder()
            .id(quoteId)
            .number("DEV-2026-0001")
            .clientId(clientId)
            .clientName(client.getName())
            .issueDate(LocalDate.of(2026, 6, 15))
            .validUntil(LocalDate.of(2026, 7, 15))
            .status(QuoteStatus.DRAFT)
            .lines(new java.util.ArrayList<>())
            .amountExclTax(BigDecimal.ZERO)
            .taxAmount(BigDecimal.ZERO)
            .totalAmount(BigDecimal.ZERO)
            .build();
  }

  private QuoteResponse dummyResponse() {
    return new QuoteResponse(
        quoteId.toString(),
        quote.getNumber(),
        clientId.toString(),
        client.getName(),
        quote.getIssueDate(),
        quote.getValidUntil(),
        List.of(),
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        quote.getStatus(),
        null,
        null,
        null,
        null);
  }

  @Test
  void createShouldRejectUnknownClient() {
    QuoteCreateDto dto =
        new QuoteCreateDto(
            clientId,
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            List.of(new QuoteLineDto("Ligne", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO)),
            null);
    when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> quoteService.create(dto))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void createShouldGenerateNumberComputeTotalsAndPersist() {
    QuoteCreateDto dto =
        new QuoteCreateDto(
            clientId,
            LocalDate.of(2026, 6, 15),
            LocalDate.of(2026, 7, 15),
            List.of(
                new QuoteLineDto(
                    "Ordinateur portable",
                    BigDecimal.valueOf(4),
                    BigDecimal.valueOf(250000),
                    BigDecimal.valueOf(18)),
                new QuoteLineDto(
                    "Installation",
                    BigDecimal.ONE,
                    BigDecimal.valueOf(200000),
                    BigDecimal.valueOf(18))),
            "Notes de test");
    when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(companySettingsService.getQuotePrefix()).thenReturn("DEV");
    when(documentNumberGenerator.next(DocumentType.QUOTE, "DEV")).thenReturn("DEV-2026-0003");
    ArgumentCaptor<Quote> captor = ArgumentCaptor.forClass(Quote.class);
    when(quoteRepository.save(captor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(quoteMapper.toResponse(any(Quote.class))).thenReturn(dummyResponse());

    quoteService.create(dto);

    Quote persisted = captor.getValue();
    assertThat(persisted.getNumber()).isEqualTo("DEV-2026-0003");
    assertThat(persisted.getClientId()).isEqualTo(clientId);
    assertThat(persisted.getClientName()).isEqualTo("ACME Senegal SARL");
    assertThat(persisted.getStatus()).isEqualTo(QuoteStatus.DRAFT);
    assertThat(persisted.getLines()).hasSize(2);
    assertThat(persisted.getLines().get(0).getLineOrder()).isZero();
    assertThat(persisted.getLines().get(1).getLineOrder()).isEqualTo(1);
    assertThat(persisted.getAmountExclTax()).isEqualByComparingTo("1200000.00");
    assertThat(persisted.getTaxAmount()).isEqualByComparingTo("216000.00");
    assertThat(persisted.getTotalAmount()).isEqualByComparingTo("1416000.00");
  }

  @Test
  void findByIdShouldThrowWhenMissing() {
    when(quoteRepository.findById(quoteId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> quoteService.findById(quoteId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void findByIdShouldReturnResponseWhenFound() {
    QuoteResponse expected = dummyResponse();
    when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
    when(quoteMapper.toResponse(quote)).thenReturn(expected);

    QuoteResponse response = quoteService.findById(quoteId);

    assertThat(response).isEqualTo(expected);
  }

  @Test
  void searchShouldMapRepositoryPageToResponsePage() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Quote> page = new PageImpl<>(List.of(quote));
    QuoteResponse expected = dummyResponse();
    when(quoteRepository.findAll(ArgumentMatchers.<Specification<Quote>>any(), eq(pageable)))
        .thenReturn(page);
    when(quoteMapper.toResponse(quote)).thenReturn(expected);

    Page<QuoteResponse> result = quoteService.search("acme", pageable);

    assertThat(result.getContent()).containsExactly(expected);
  }

  @Test
  void updateShouldKeepClientWhenUnchangedAndReplaceLines() {
    QuoteUpdateDto dto =
        new QuoteUpdateDto(
            clientId,
            LocalDate.of(2026, 6, 16),
            LocalDate.of(2026, 7, 16),
            List.of(
                new QuoteLineDto(
                    "Nouvelle ligne", BigDecimal.TEN, BigDecimal.valueOf(1000), BigDecimal.ZERO)),
            "Mise a jour",
            QuoteStatus.SENT);
    when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
    when(quoteRepository.save(quote)).thenReturn(quote);
    when(quoteMapper.toResponse(quote)).thenReturn(dummyResponse());

    quoteService.update(quoteId, dto);

    verify(clientRepository, org.mockito.Mockito.never()).findById(any());
    assertThat(quote.getStatus()).isEqualTo(QuoteStatus.SENT);
    assertThat(quote.getLines()).hasSize(1);
    assertThat(quote.getAmountExclTax()).isEqualByComparingTo("10000.00");
  }

  @Test
  void updateShouldReloadClientWhenClientIdChanges() {
    UUID newClientId = UUID.randomUUID();
    Client newClient =
        Client.builder()
            .id(newClientId)
            .name("Baobab Distribution")
            .status(ClientStatus.ACTIVE)
            .build();
    QuoteUpdateDto dto =
        new QuoteUpdateDto(
            newClientId,
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            List.of(new QuoteLineDto("Ligne", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO)),
            null,
            QuoteStatus.DRAFT);
    when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
    when(clientRepository.findById(newClientId)).thenReturn(Optional.of(newClient));
    when(quoteRepository.save(quote)).thenReturn(quote);
    when(quoteMapper.toResponse(quote)).thenReturn(dummyResponse());

    quoteService.update(quoteId, dto);

    assertThat(quote.getClientId()).isEqualTo(newClientId);
    assertThat(quote.getClientName()).isEqualTo("Baobab Distribution");
  }

  @Test
  void updateShouldThrowWhenQuoteMissing() {
    QuoteUpdateDto dto =
        new QuoteUpdateDto(
            clientId,
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            List.of(new QuoteLineDto("Ligne", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO)),
            null,
            QuoteStatus.DRAFT);
    when(quoteRepository.findById(quoteId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> quoteService.update(quoteId, dto))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void deleteShouldRemoveExistingQuote() {
    when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));

    quoteService.delete(quoteId);

    verify(quoteRepository).delete(quote);
  }

  @Test
  void deleteShouldThrowWhenQuoteMissing() {
    when(quoteRepository.findById(quoteId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> quoteService.delete(quoteId))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
