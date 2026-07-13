package com.facturationpme.invoices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.facturationpme.clients.domain.Client;
import com.facturationpme.clients.domain.ClientStatus;
import com.facturationpme.clients.repository.ClientRepository;
import com.facturationpme.common.exception.InvalidStateException;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.common.numbering.DocumentNumberGenerator;
import com.facturationpme.common.numbering.DocumentType;
import com.facturationpme.invoices.domain.Invoice;
import com.facturationpme.invoices.domain.InvoiceStatus;
import com.facturationpme.invoices.dto.InvoiceCreateDto;
import com.facturationpme.invoices.dto.InvoiceLineDto;
import com.facturationpme.invoices.dto.InvoiceResponse;
import com.facturationpme.invoices.dto.InvoiceUpdateDto;
import com.facturationpme.invoices.event.InvoiceValidatedEvent;
import com.facturationpme.invoices.mapper.InvoiceMapper;
import com.facturationpme.invoices.repository.InvoiceRepository;
import com.facturationpme.invoices.service.InvoiceService;
import com.facturationpme.settings.service.CompanySettingsService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class InvoiceServiceTest {

  @Mock private InvoiceRepository invoiceRepository;
  @Mock private ClientRepository clientRepository;
  @Mock private InvoiceMapper invoiceMapper;
  @Mock private DocumentNumberGenerator documentNumberGenerator;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private CompanySettingsService companySettingsService;

  @InjectMocks private InvoiceService invoiceService;

  private Client client;
  private UUID clientId;
  private UUID invoiceId;
  private Invoice invoice;

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

    invoiceId = UUID.randomUUID();
    invoice =
        Invoice.builder()
            .id(invoiceId)
            .number("FAC-2026-0001")
            .clientId(clientId)
            .clientName(client.getName())
            .issueDate(LocalDate.of(2026, 5, 1))
            .dueDate(LocalDate.of(2026, 5, 31))
            .status(InvoiceStatus.DRAFT)
            .lines(new ArrayList<>())
            .amountExclTax(BigDecimal.ZERO)
            .taxAmount(BigDecimal.ZERO)
            .totalAmount(BigDecimal.ZERO)
            .build();
  }

  private InvoiceResponse dummyResponse(InvoiceStatus status) {
    return new InvoiceResponse(
        invoiceId.toString(),
        invoice.getNumber(),
        clientId.toString(),
        client.getName(),
        invoice.getIssueDate(),
        invoice.getDueDate(),
        List.of(),
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        status,
        null,
        null,
        null);
  }

  @Test
  void createShouldRejectUnknownClient() {
    InvoiceCreateDto dto =
        new InvoiceCreateDto(
            clientId,
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            List.of(new InvoiceLineDto("Ligne", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO)),
            null);
    when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> invoiceService.create(dto))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void createShouldGenerateNumberComputeTotalsAndPersistAsDraft() {
    InvoiceCreateDto dto =
        new InvoiceCreateDto(
            clientId,
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 5, 31),
            List.of(
                new InvoiceLineDto(
                    "Prestation",
                    BigDecimal.ONE,
                    BigDecimal.valueOf(900000),
                    BigDecimal.valueOf(18))),
            null);
    when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(companySettingsService.getInvoicePrefix()).thenReturn("FAC");
    when(documentNumberGenerator.next(DocumentType.INVOICE, "FAC")).thenReturn("FAC-2026-0003");
    when(invoiceRepository.save(any(Invoice.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(invoiceMapper.toResponse(any(Invoice.class)))
        .thenReturn(dummyResponse(InvoiceStatus.DRAFT));

    InvoiceResponse response = invoiceService.create(dto);

    assertThat(response.status()).isEqualTo(InvoiceStatus.DRAFT);
  }

  @Test
  void findByIdShouldThrowWhenMissing() {
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> invoiceService.findById(invoiceId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void findByIdShouldReturnRawStatusWhenNotOverdue() {
    invoice.setStatus(InvoiceStatus.SENT);
    invoice.setDueDate(LocalDate.now().plusDays(10));
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(invoiceMapper.toResponse(invoice)).thenReturn(dummyResponse(InvoiceStatus.SENT));

    InvoiceResponse response = invoiceService.findById(invoiceId);

    assertThat(response.status()).isEqualTo(InvoiceStatus.SENT);
  }

  @Test
  void findByIdShouldComputeOverdueWhenSentAndPastDueDate() {
    invoice.setStatus(InvoiceStatus.SENT);
    invoice.setDueDate(LocalDate.now().minusDays(1));
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(invoiceMapper.toResponse(invoice)).thenReturn(dummyResponse(InvoiceStatus.SENT));

    InvoiceResponse response = invoiceService.findById(invoiceId);

    assertThat(response.status()).isEqualTo(InvoiceStatus.OVERDUE);
  }

  @Test
  void findByIdShouldNotComputeOverdueWhenAlreadyPaidPastDueDate() {
    invoice.setStatus(InvoiceStatus.PAID);
    invoice.setDueDate(LocalDate.now().minusDays(30));
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(invoiceMapper.toResponse(invoice)).thenReturn(dummyResponse(InvoiceStatus.PAID));

    InvoiceResponse response = invoiceService.findById(invoiceId);

    assertThat(response.status()).isEqualTo(InvoiceStatus.PAID);
  }

  @Test
  void searchShouldMapRepositoryPageToResponsePage() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Invoice> page = new PageImpl<>(List.of(invoice));
    when(invoiceRepository.findAll(ArgumentMatchers.<Specification<Invoice>>any(), eq(pageable)))
        .thenReturn(page);
    when(invoiceMapper.toResponse(invoice)).thenReturn(dummyResponse(InvoiceStatus.DRAFT));

    Page<InvoiceResponse> result = invoiceService.search("acme", pageable);

    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  void updateShouldReplaceLinesAndStatus() {
    InvoiceUpdateDto dto =
        new InvoiceUpdateDto(
            clientId,
            LocalDate.of(2026, 5, 2),
            LocalDate.of(2026, 6, 1),
            List.of(
                new InvoiceLineDto(
                    "Nouvelle ligne", BigDecimal.TEN, BigDecimal.valueOf(1000), BigDecimal.ZERO)),
            "Mise a jour",
            InvoiceStatus.SENT);
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(invoiceRepository.save(invoice)).thenReturn(invoice);
    when(invoiceMapper.toResponse(invoice)).thenReturn(dummyResponse(InvoiceStatus.SENT));

    invoiceService.update(invoiceId, dto);

    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
    assertThat(invoice.getLines()).hasSize(1);
    assertThat(invoice.getAmountExclTax()).isEqualByComparingTo("10000.00");
  }

  @Test
  void updateShouldThrowWhenInvoiceMissing() {
    InvoiceUpdateDto dto =
        new InvoiceUpdateDto(
            clientId,
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            List.of(new InvoiceLineDto("Ligne", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO)),
            null,
            InvoiceStatus.DRAFT);
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> invoiceService.update(invoiceId, dto))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void deleteShouldRemoveExistingInvoice() {
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

    invoiceService.delete(invoiceId);

    verify(invoiceRepository).delete(invoice);
  }

  @Test
  void deleteShouldThrowWhenInvoiceMissing() {
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> invoiceService.delete(invoiceId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void validateShouldTransitionDraftToSentAndPublishEvent() {
    invoice.setDueDate(LocalDate.now().plusDays(30));
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(invoiceRepository.save(invoice)).thenReturn(invoice);
    when(invoiceMapper.toResponse(invoice)).thenReturn(dummyResponse(InvoiceStatus.SENT));

    InvoiceResponse response = invoiceService.validate(invoiceId);

    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
    assertThat(response.status()).isEqualTo(InvoiceStatus.SENT);
    verify(eventPublisher).publishEvent(any(InvoiceValidatedEvent.class));
  }

  @Test
  void validateShouldBeIdempotentAndNotRepublishEventWhenAlreadySent() {
    invoice.setStatus(InvoiceStatus.SENT);
    invoice.setDueDate(LocalDate.now().plusDays(30));
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(invoiceMapper.toResponse(invoice)).thenReturn(dummyResponse(InvoiceStatus.SENT));

    invoiceService.validate(invoiceId);

    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
    verify(invoiceRepository, never()).save(any(Invoice.class));
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void validateShouldNotResetPartiallyPaidInvoiceBackToSent() {
    invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
    invoice.setDueDate(LocalDate.now().plusDays(30));
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(invoiceMapper.toResponse(invoice)).thenReturn(dummyResponse(InvoiceStatus.PARTIALLY_PAID));

    invoiceService.validate(invoiceId);

    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void validateShouldRejectAlreadyPaidInvoice() {
    invoice.setStatus(InvoiceStatus.PAID);
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

    assertThatThrownBy(() -> invoiceService.validate(invoiceId))
        .isInstanceOf(InvalidStateException.class);
  }

  @Test
  void validateShouldRejectCancelledInvoice() {
    invoice.setStatus(InvoiceStatus.CANCELLED);
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

    assertThatThrownBy(() -> invoiceService.validate(invoiceId))
        .isInstanceOf(InvalidStateException.class);
  }

  @Test
  void sendShouldTransitionDraftToSent() {
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(invoiceRepository.save(invoice)).thenReturn(invoice);
    when(invoiceMapper.toResponse(invoice)).thenReturn(dummyResponse(InvoiceStatus.SENT));

    invoiceService.send(invoiceId);

    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
  }
}
