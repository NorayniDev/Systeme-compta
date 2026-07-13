package com.facturationpme.suppliers.web;

import com.facturationpme.common.dto.PageResponse;
import com.facturationpme.common.util.PageableFactory;
import com.facturationpme.suppliers.dto.SupplierCreateDto;
import com.facturationpme.suppliers.dto.SupplierResponse;
import com.facturationpme.suppliers.dto.SupplierUpdateDto;
import com.facturationpme.suppliers.service.SupplierService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

  private final SupplierService supplierService;

  @GetMapping
  public PageResponse<SupplierResponse> search(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(required = false) String query) {
    Pageable pageable = PageableFactory.of(page, size, sort, direction);
    return PageResponse.of(supplierService.search(query, pageable));
  }

  @GetMapping("/{id}")
  public SupplierResponse findById(@PathVariable UUID id) {
    return supplierService.findById(id);
  }

  @PostMapping
  @PreAuthorize("hasAuthority('supplier:manage')")
  public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierCreateDto dto) {
    return ResponseEntity.status(201).body(supplierService.create(dto));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('supplier:manage')")
  public SupplierResponse update(@PathVariable UUID id, @Valid @RequestBody SupplierUpdateDto dto) {
    return supplierService.update(id, dto);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('supplier:manage')")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    supplierService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
