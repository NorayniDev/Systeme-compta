package com.facturationpme.invoices.service;

import com.facturationpme.audit.domain.AuditAction;
import com.facturationpme.audit.event.AuditableActionEvent;
import com.facturationpme.clients.domain.Client;
import com.facturationpme.clients.repository.ClientRepository;
import com.facturationpme.common.exception.InvalidStateException;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.common.numbering.DocumentNumberGenerator;
import com.facturationpme.common.numbering.DocumentType;
import com.facturationpme.common.security.SecurityUtils;
import com.facturationpme.common.util.LineItemCalculator;
import com.facturationpme.invoices.domain.Invoice;
import com.facturationpme.invoices.domain.InvoiceLine;
import com.facturationpme.invoices.domain.InvoiceStatus;
import com.facturationpme.invoices.dto.InvoiceCreateDto;
import com.facturationpme.invoices.dto.InvoiceLineDto;
import com.facturationpme.invoices.dto.InvoiceResponse;
import com.facturationpme.invoices.dto.InvoiceUpdateDto;
import com.facturationpme.invoices.event.InvoiceValidatedEvent;
import com.facturationpme.invoices.mapper.InvoiceMapper;
import com.facturationpme.invoices.repository.InvoiceRepository;
import com.facturationpme.invoices.repository.InvoiceSpecifications;
import com.facturationpme.settings.service.CompanySettingsService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceService {

  /**
   * Statuts au-dela desquels une facture est definitivement cloturee - plus aucune transition
   * possible.
   */
  private static final Set<InvoiceStatus> TERMINAL_STATUSES =
      EnumSet.of(InvoiceStatus.PAID, InvoiceStatus.CANCELLED);

  /**
   * Statuts pour lesquels l'echeance depassee bascule l'affichage en OVERDUE (calcule a la lecture,
   * non persiste).
   */
  private static final Set<InvoiceStatus> OVERDUE_ELIGIBLE_STATUSES =
      EnumSet.of(InvoiceStatus.SENT, InvoiceStatus.PARTIALLY_PAID);

  private final InvoiceRepository invoiceRepository;
  private final ClientRepository clientRepository;
  private final InvoiceMapper invoiceMapper;
  private final DocumentNumberGenerator documentNumberGenerator;
  private final ApplicationEventPublisher eventPublisher;
  private final CompanySettingsService companySettingsService;

  @Transactional(readOnly = true)
  public Page<InvoiceResponse> search(String query, Pageable pageable) {
    Specification<Invoice> specification =
        Specification.where(InvoiceSpecifications.matchingQuery(query));
    return invoiceRepository
        .findAll(specification, pageable)
        .map(this::toResponseWithComputedStatus);
  }

  @Transactional(readOnly = true)
  public InvoiceResponse findById(UUID id) {
    return toResponseWithComputedStatus(getOrThrow(id));
  }

  @Transactional
  public InvoiceResponse create(InvoiceCreateDto dto) {
    Client client = findClientOrThrow(dto.clientId());

    Invoice invoice =
        Invoice.builder()
            .number(
                documentNumberGenerator.next(
                    DocumentType.INVOICE, companySettingsService.getInvoicePrefix()))
            .clientId(client.getId())
            .clientName(client.getName())
            .issueDate(dto.issueDate())
            .dueDate(dto.dueDate())
            .status(InvoiceStatus.DRAFT)
            .notes(dto.notes())
            .lines(new ArrayList<>())
            .build();

    applyLines(invoice, dto.lines());

    Invoice saved = invoiceRepository.save(invoice);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.CREATE, "Facture", saved.getNumber(), SecurityUtils.currentUserId()));
    return toResponseWithComputedStatus(saved);
  }

  @Transactional
  public InvoiceResponse update(UUID id, InvoiceUpdateDto dto) {
    Invoice invoice = getOrThrow(id);

    if (!invoice.getClientId().equals(dto.clientId())) {
      Client client = findClientOrThrow(dto.clientId());
      invoice.setClientId(client.getId());
      invoice.setClientName(client.getName());
    }

    invoice.setIssueDate(dto.issueDate());
    invoice.setDueDate(dto.dueDate());
    invoice.setNotes(dto.notes());
    invoice.setStatus(dto.status());
    invoice.getLines().clear();
    applyLines(invoice, dto.lines());

    Invoice saved = invoiceRepository.save(invoice);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.UPDATE, "Facture", saved.getNumber(), SecurityUtils.currentUserId()));
    return toResponseWithComputedStatus(saved);
  }

  @Transactional
  public void delete(UUID id) {
    Invoice invoice = getOrThrow(id);
    invoiceRepository.delete(invoice);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.DELETE, "Facture", invoice.getNumber(), SecurityUtils.currentUserId()));
  }

  /**
   * Fait passer une facture brouillon a l'etat envoye. Que ce soit via la validation comptable ou
   * l'envoi effectif au client, le mock frontend historique traite les deux actions de facon
   * identique (transition unique DRAFT -> SENT) ; seule differe la permission requise pour y
   * acceder (voir InvoiceController).
   */
  @Transactional
  public InvoiceResponse validate(UUID id) {
    return transitionToSent(id);
  }

  @Transactional
  public InvoiceResponse send(UUID id) {
    return transitionToSent(id);
  }

  /**
   * N'agit que si la facture est encore DRAFT : rappeler validate/send sur une facture deja
   * SENT/PARTIALLY_PAID doit rester un no-op idempotent, jamais reinitialiser son statut (on
   * perdrait sinon la trace d'un paiement partiel deja enregistre) ni republier {@link
   * InvoiceValidatedEvent} une seconde fois (doublerait les ecritures comptables).
   */
  private InvoiceResponse transitionToSent(UUID id) {
    Invoice invoice = getOrThrow(id);
    if (TERMINAL_STATUSES.contains(invoice.getStatus())) {
      throw new InvalidStateException(
          "Cette facture est definitivement cloturee ("
              + invoice.getStatus()
              + ") et ne peut plus etre modifiee.");
    }
    if (invoice.getStatus() == InvoiceStatus.DRAFT) {
      invoice.setStatus(InvoiceStatus.SENT);
      invoiceRepository.save(invoice);
      eventPublisher.publishEvent(
          new InvoiceValidatedEvent(
              invoice.getId(),
              invoice.getNumber(),
              invoice.getClientName(),
              invoice.getIssueDate(),
              invoice.getAmountExclTax(),
              invoice.getTaxAmount(),
              invoice.getTotalAmount()));
      eventPublisher.publishEvent(
          new AuditableActionEvent(
              AuditAction.VALIDATE, "Facture", invoice.getNumber(), SecurityUtils.currentUserId()));
    }
    return toResponseWithComputedStatus(invoice);
  }

  private void applyLines(Invoice invoice, List<InvoiceLineDto> lineDtos) {
    List<LineItemCalculator.LineInput> inputs =
        lineDtos.stream().map(InvoiceService::toLineInput).toList();
    LineItemCalculator.Totals totals = LineItemCalculator.computeTotals(inputs);

    for (int index = 0; index < lineDtos.size(); index++) {
      InvoiceLineDto lineDto = lineDtos.get(index);
      InvoiceLine line =
          InvoiceLine.builder()
              .invoice(invoice)
              .description(lineDto.description())
              .quantity(lineDto.quantity())
              .unitPrice(lineDto.unitPrice())
              .taxRate(lineDto.taxRate())
              .lineTotal(LineItemCalculator.computeLineTotal(toLineInput(lineDto)))
              .lineOrder(index)
              .build();
      invoice.getLines().add(line);
    }

    invoice.setAmountExclTax(totals.amountExclTax());
    invoice.setTaxAmount(totals.taxAmount());
    invoice.setTotalAmount(totals.totalAmount());
  }

  private static LineItemCalculator.LineInput toLineInput(InvoiceLineDto dto) {
    return new LineItemCalculator.LineInput(dto.quantity(), dto.unitPrice(), dto.taxRate());
  }

  /** Calcule le statut OVERDUE a la volee (echeance depassee) sans le persister - voir spec §6. */
  private InvoiceResponse toResponseWithComputedStatus(Invoice invoice) {
    InvoiceResponse response = invoiceMapper.toResponse(invoice);
    boolean isOverdue =
        OVERDUE_ELIGIBLE_STATUSES.contains(invoice.getStatus())
            && invoice.getDueDate().isBefore(LocalDate.now());
    return isOverdue ? response.withStatus(InvoiceStatus.OVERDUE) : response;
  }

  private Client findClientOrThrow(UUID clientId) {
    return clientRepository
        .findById(clientId)
        .orElseThrow(() -> ResourceNotFoundException.of("Client", clientId));
  }

  private Invoice getOrThrow(UUID id) {
    return invoiceRepository
        .findById(id)
        .orElseThrow(() -> ResourceNotFoundException.of("Facture", id));
  }
}
