package com.facturationpme.services.web;

import com.facturationpme.common.dto.PageResponse;
import com.facturationpme.common.util.PageableFactory;
import com.facturationpme.services.dto.ServiceItemCreateDto;
import com.facturationpme.services.dto.ServiceItemResponse;
import com.facturationpme.services.dto.ServiceItemUpdateDto;
import com.facturationpme.services.service.ServiceItemService;
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
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceItemController {

  private final ServiceItemService serviceItemService;

  @GetMapping
  public PageResponse<ServiceItemResponse> search(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(required = false) String query) {
    Pageable pageable = PageableFactory.of(page, size, sort, direction);
    return PageResponse.of(serviceItemService.search(query, pageable));
  }

  @GetMapping("/{id}")
  public ServiceItemResponse findById(@PathVariable UUID id) {
    return serviceItemService.findById(id);
  }

  @PostMapping
  @PreAuthorize("hasAuthority('service:manage')")
  public ResponseEntity<ServiceItemResponse> create(@Valid @RequestBody ServiceItemCreateDto dto) {
    return ResponseEntity.status(201).body(serviceItemService.create(dto));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('service:manage')")
  public ServiceItemResponse update(
      @PathVariable UUID id, @Valid @RequestBody ServiceItemUpdateDto dto) {
    return serviceItemService.update(id, dto);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('service:manage')")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    serviceItemService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
