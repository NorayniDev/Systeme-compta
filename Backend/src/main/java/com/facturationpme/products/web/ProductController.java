package com.facturationpme.products.web;

import com.facturationpme.common.dto.PageResponse;
import com.facturationpme.common.util.PageableFactory;
import com.facturationpme.products.dto.ProductCreateDto;
import com.facturationpme.products.dto.ProductResponse;
import com.facturationpme.products.dto.ProductUpdateDto;
import com.facturationpme.products.service.ProductService;
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
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping
  public PageResponse<ProductResponse> search(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(required = false) String query) {
    Pageable pageable = PageableFactory.of(page, size, sort, direction);
    return PageResponse.of(productService.search(query, pageable));
  }

  @GetMapping("/{id}")
  public ProductResponse findById(@PathVariable UUID id) {
    return productService.findById(id);
  }

  @PostMapping
  @PreAuthorize("hasAuthority('product:manage')")
  public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateDto dto) {
    return ResponseEntity.status(201).body(productService.create(dto));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('product:manage')")
  public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductUpdateDto dto) {
    return productService.update(id, dto);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('product:manage')")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    productService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
