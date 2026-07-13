package com.facturationpme.quotes.web;

import com.facturationpme.common.dto.PageResponse;
import com.facturationpme.common.util.PageableFactory;
import com.facturationpme.pdf.service.QuotePdfService;
import com.facturationpme.quotes.dto.QuoteCreateDto;
import com.facturationpme.quotes.dto.QuoteResponse;
import com.facturationpme.quotes.dto.QuoteUpdateDto;
import com.facturationpme.quotes.service.QuoteService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrairement a Clients/Suppliers/Products/Services (une seule permission {@code xxx:manage}),
 * les Devis exposent des permissions granulaires (create/read/update/delete) - certains roles (ex:
 * CAISSIER) n'ont aucun acces, meme en lecture. Voir {@code RolePermissions}.
 */
@RestController
@RequestMapping("/quotes")
@RequiredArgsConstructor
public class QuoteController {

  private final QuoteService quoteService;
  private final QuotePdfService quotePdfService;

  @GetMapping
  @PreAuthorize("hasAuthority('quote:read')")
  public PageResponse<QuoteResponse> search(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(required = false) String query) {
    Pageable pageable = PageableFactory.of(page, size, sort, direction);
    return PageResponse.of(quoteService.search(query, pageable));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('quote:read')")
  public QuoteResponse findById(@PathVariable UUID id) {
    return quoteService.findById(id);
  }

  @PostMapping
  @PreAuthorize("hasAuthority('quote:create')")
  public ResponseEntity<QuoteResponse> create(@Valid @RequestBody QuoteCreateDto dto) {
    return ResponseEntity.status(201).body(quoteService.create(dto));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('quote:update')")
  public QuoteResponse update(@PathVariable UUID id, @Valid @RequestBody QuoteUpdateDto dto) {
    return quoteService.update(id, dto);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('quote:delete')")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    quoteService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  @PreAuthorize("hasAuthority('quote:read')")
  public ResponseEntity<byte[]> pdf(@PathVariable UUID id) {
    var pdf = quotePdfService.generate(id);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdf.fileName() + "\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf.content());
  }
}
