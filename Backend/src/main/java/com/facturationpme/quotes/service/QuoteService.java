package com.facturationpme.quotes.service;

import com.facturationpme.audit.domain.AuditAction;
import com.facturationpme.audit.event.AuditableActionEvent;
import com.facturationpme.clients.domain.Client;
import com.facturationpme.clients.repository.ClientRepository;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.common.numbering.DocumentNumberGenerator;
import com.facturationpme.common.numbering.DocumentType;
import com.facturationpme.common.security.SecurityUtils;
import com.facturationpme.common.util.LineItemCalculator;
import com.facturationpme.quotes.domain.Quote;
import com.facturationpme.quotes.domain.QuoteLine;
import com.facturationpme.quotes.domain.QuoteStatus;
import com.facturationpme.quotes.dto.QuoteCreateDto;
import com.facturationpme.quotes.dto.QuoteLineDto;
import com.facturationpme.quotes.dto.QuoteResponse;
import com.facturationpme.quotes.dto.QuoteUpdateDto;
import com.facturationpme.quotes.mapper.QuoteMapper;
import com.facturationpme.quotes.repository.QuoteRepository;
import com.facturationpme.quotes.repository.QuoteSpecifications;
import com.facturationpme.settings.service.CompanySettingsService;
import java.util.ArrayList;
import java.util.List;
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
public class QuoteService {

  private final QuoteRepository quoteRepository;
  private final ClientRepository clientRepository;
  private final QuoteMapper quoteMapper;
  private final DocumentNumberGenerator documentNumberGenerator;
  private final CompanySettingsService companySettingsService;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public Page<QuoteResponse> search(String query, Pageable pageable) {
    Specification<Quote> specification =
        Specification.where(QuoteSpecifications.matchingQuery(query));
    return quoteRepository.findAll(specification, pageable).map(quoteMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public QuoteResponse findById(UUID id) {
    return quoteMapper.toResponse(getOrThrow(id));
  }

  @Transactional
  public QuoteResponse create(QuoteCreateDto dto) {
    Client client = findClientOrThrow(dto.clientId());

    Quote quote =
        Quote.builder()
            .number(
                documentNumberGenerator.next(
                    DocumentType.QUOTE, companySettingsService.getQuotePrefix()))
            .clientId(client.getId())
            .clientName(client.getName())
            .issueDate(dto.issueDate())
            .validUntil(dto.validUntil())
            .status(QuoteStatus.DRAFT)
            .notes(dto.notes())
            .lines(new ArrayList<>())
            .build();

    applyLines(quote, dto.lines());

    Quote saved = quoteRepository.save(quote);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.CREATE, "Devis", saved.getNumber(), SecurityUtils.currentUserId()));
    return quoteMapper.toResponse(saved);
  }

  @Transactional
  public QuoteResponse update(UUID id, QuoteUpdateDto dto) {
    Quote quote = getOrThrow(id);

    if (!quote.getClientId().equals(dto.clientId())) {
      Client client = findClientOrThrow(dto.clientId());
      quote.setClientId(client.getId());
      quote.setClientName(client.getName());
    }

    quote.setIssueDate(dto.issueDate());
    quote.setValidUntil(dto.validUntil());
    quote.setNotes(dto.notes());
    quote.setStatus(dto.status());
    quote.getLines().clear();
    applyLines(quote, dto.lines());

    Quote saved = quoteRepository.save(quote);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.UPDATE, "Devis", saved.getNumber(), SecurityUtils.currentUserId()));
    return quoteMapper.toResponse(saved);
  }

  @Transactional
  public void delete(UUID id) {
    Quote quote = getOrThrow(id);
    quoteRepository.delete(quote);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.DELETE, "Devis", quote.getNumber(), SecurityUtils.currentUserId()));
  }

  private void applyLines(Quote quote, List<QuoteLineDto> lineDtos) {
    List<LineItemCalculator.LineInput> inputs =
        lineDtos.stream().map(QuoteService::toLineInput).toList();
    LineItemCalculator.Totals totals = LineItemCalculator.computeTotals(inputs);

    for (int index = 0; index < lineDtos.size(); index++) {
      QuoteLineDto lineDto = lineDtos.get(index);
      QuoteLine line =
          QuoteLine.builder()
              .quote(quote)
              .description(lineDto.description())
              .quantity(lineDto.quantity())
              .unitPrice(lineDto.unitPrice())
              .taxRate(lineDto.taxRate())
              .lineTotal(LineItemCalculator.computeLineTotal(toLineInput(lineDto)))
              .lineOrder(index)
              .build();
      quote.getLines().add(line);
    }

    quote.setAmountExclTax(totals.amountExclTax());
    quote.setTaxAmount(totals.taxAmount());
    quote.setTotalAmount(totals.totalAmount());
  }

  private static LineItemCalculator.LineInput toLineInput(QuoteLineDto dto) {
    return new LineItemCalculator.LineInput(dto.quantity(), dto.unitPrice(), dto.taxRate());
  }

  private Client findClientOrThrow(UUID clientId) {
    return clientRepository
        .findById(clientId)
        .orElseThrow(() -> ResourceNotFoundException.of("Client", clientId));
  }

  private Quote getOrThrow(UUID id) {
    return quoteRepository
        .findById(id)
        .orElseThrow(() -> ResourceNotFoundException.of("Devis", id));
  }
}
