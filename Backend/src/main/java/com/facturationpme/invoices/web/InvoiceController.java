package com.facturationpme.invoices.web;

import com.facturationpme.common.dto.PageResponse;
import com.facturationpme.common.util.PageableFactory;
import com.facturationpme.invoices.dto.InvoiceCreateDto;
import com.facturationpme.invoices.dto.InvoiceResponse;
import com.facturationpme.invoices.dto.InvoiceUpdateDto;
import com.facturationpme.invoices.service.InvoiceService;
import com.facturationpme.pdf.service.InvoicePdfService;
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

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

  private final InvoiceService invoiceService;
  private final InvoicePdfService invoicePdfService;

  @GetMapping
  @PreAuthorize("hasAuthority('invoice:read')")
  public PageResponse<InvoiceResponse> search(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(required = false) String query) {
    Pageable pageable = PageableFactory.of(page, size, sort, direction);
    return PageResponse.of(invoiceService.search(query, pageable));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('invoice:read')")
  public InvoiceResponse findById(@PathVariable UUID id) {
    return invoiceService.findById(id);
  }

  @PostMapping
  @PreAuthorize("hasAuthority('invoice:create')")
  public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceCreateDto dto) {
    return ResponseEntity.status(201).body(invoiceService.create(dto));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('invoice:update')")
  public InvoiceResponse update(@PathVariable UUID id, @Valid @RequestBody InvoiceUpdateDto dto) {
    return invoiceService.update(id, dto);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('invoice:delete')")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    invoiceService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/validate")
  @PreAuthorize("hasAuthority('invoice:validate')")
  public InvoiceResponse validate(@PathVariable UUID id) {
    return invoiceService.validate(id);
  }

  @PostMapping("/{id}/send")
  @PreAuthorize("hasAuthority('invoice:validate')")
  public InvoiceResponse send(@PathVariable UUID id) {
    return invoiceService.send(id);
  }

  @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  @PreAuthorize("hasAuthority('invoice:read')")
  public ResponseEntity<byte[]> pdf(@PathVariable UUID id) {
    var pdf = invoicePdfService.generate(id);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdf.fileName() + "\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf.content());
  }
}
