package com.facturationpme.clients.web;

import com.facturationpme.clients.dto.ClientCreateDto;
import com.facturationpme.clients.dto.ClientResponse;
import com.facturationpme.clients.dto.ClientUpdateDto;
import com.facturationpme.clients.service.ClientService;
import com.facturationpme.common.dto.PageResponse;
import com.facturationpme.common.util.PageableFactory;
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
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

  private final ClientService clientService;

  @GetMapping
  public PageResponse<ClientResponse> search(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(required = false) String query) {
    Pageable pageable = PageableFactory.of(page, size, sort, direction);
    return PageResponse.of(clientService.search(query, pageable));
  }

  @GetMapping("/{id}")
  public ClientResponse findById(@PathVariable UUID id) {
    return clientService.findById(id);
  }

  @PostMapping
  @PreAuthorize("hasAuthority('client:manage')")
  public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientCreateDto dto) {
    return ResponseEntity.status(201).body(clientService.create(dto));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('client:manage')")
  public ClientResponse update(@PathVariable UUID id, @Valid @RequestBody ClientUpdateDto dto) {
    return clientService.update(id, dto);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('client:manage')")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    clientService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
