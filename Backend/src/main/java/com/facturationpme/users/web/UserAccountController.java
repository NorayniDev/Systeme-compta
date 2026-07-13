package com.facturationpme.users.web;

import com.facturationpme.common.dto.PageResponse;
import com.facturationpme.common.util.PageableFactory;
import com.facturationpme.users.dto.UserAccountCreateDto;
import com.facturationpme.users.dto.UserAccountUpdateDto;
import com.facturationpme.users.dto.UserResponse;
import com.facturationpme.users.service.UserAccountService;
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

/**
 * Gestion des comptes utilisateurs, reservee aux administrateurs (voir {@code RolePermissions} :
 * seul {@code ADMIN} possede {@code user:manage}). Les roles restent une enumeration fixe cote
 * frontend (pas de CRUD sur les roles) - voir {@code RoleMatrix} cote Angular.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('user:manage')")
public class UserAccountController {

  private final UserAccountService userAccountService;

  @GetMapping
  public PageResponse<UserResponse> search(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(required = false) String query) {
    Pageable pageable = PageableFactory.of(page, size, toEntityProperty(sort), direction);
    return PageResponse.of(userAccountService.search(query, pageable));
  }

  /** {@code IUser.isActive} (contrat frontend) correspond a {@code User.active} cote entite. */
  private static String toEntityProperty(String sort) {
    return "isActive".equals(sort) ? "active" : sort;
  }

  @GetMapping("/{id}")
  public UserResponse findById(@PathVariable UUID id) {
    return userAccountService.findById(id);
  }

  @PostMapping
  public ResponseEntity<UserResponse> create(@Valid @RequestBody UserAccountCreateDto dto) {
    return ResponseEntity.status(201).body(userAccountService.create(dto));
  }

  @PutMapping("/{id}")
  public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UserAccountUpdateDto dto) {
    return userAccountService.update(id, dto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    userAccountService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/reset-password")
  public ResponseEntity<Void> resetPassword(@PathVariable UUID id) {
    userAccountService.resetPassword(id);
    return ResponseEntity.noContent().build();
  }
}
