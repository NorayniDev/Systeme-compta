package com.facturationpme.suppliers.service;

import com.facturationpme.audit.domain.AuditAction;
import com.facturationpme.audit.event.AuditableActionEvent;
import com.facturationpme.common.exception.DuplicateResourceException;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.common.security.SecurityUtils;
import com.facturationpme.suppliers.domain.Supplier;
import com.facturationpme.suppliers.domain.SupplierStatus;
import com.facturationpme.suppliers.dto.SupplierCreateDto;
import com.facturationpme.suppliers.dto.SupplierResponse;
import com.facturationpme.suppliers.dto.SupplierUpdateDto;
import com.facturationpme.suppliers.mapper.SupplierMapper;
import com.facturationpme.suppliers.repository.SupplierRepository;
import com.facturationpme.suppliers.repository.SupplierSpecifications;
import java.math.BigDecimal;
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
public class SupplierService {

  private final SupplierRepository supplierRepository;
  private final SupplierMapper supplierMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public Page<SupplierResponse> search(String query, Pageable pageable) {
    Specification<Supplier> specification =
        Specification.where(SupplierSpecifications.matchingQuery(query));
    return supplierRepository.findAll(specification, pageable).map(supplierMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public SupplierResponse findById(UUID id) {
    return supplierMapper.toResponse(getOrThrow(id));
  }

  @Transactional
  public SupplierResponse create(SupplierCreateDto dto) {
    if (supplierRepository.existsByEmailIgnoreCase(dto.email())) {
      throw new DuplicateResourceException(
          "Un fournisseur utilise deja l'adresse email : " + dto.email());
    }
    Supplier supplier = supplierMapper.toEntity(dto);
    supplier.setStatus(SupplierStatus.ACTIVE);
    supplier.setTotalPurchased(BigDecimal.ZERO);
    Supplier saved = supplierRepository.save(supplier);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.CREATE, "Fournisseur", saved.getName(), SecurityUtils.currentUserId()));
    return supplierMapper.toResponse(saved);
  }

  @Transactional
  public SupplierResponse update(UUID id, SupplierUpdateDto dto) {
    Supplier supplier = getOrThrow(id);
    if (!supplier.getEmail().equalsIgnoreCase(dto.email())
        && supplierRepository.existsByEmailIgnoreCase(dto.email())) {
      throw new DuplicateResourceException(
          "Un fournisseur utilise deja l'adresse email : " + dto.email());
    }
    supplierMapper.updateEntityFromDto(dto, supplier);
    Supplier saved = supplierRepository.save(supplier);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.UPDATE, "Fournisseur", saved.getName(), SecurityUtils.currentUserId()));
    return supplierMapper.toResponse(saved);
  }

  @Transactional
  public void delete(UUID id) {
    Supplier supplier = getOrThrow(id);
    supplierRepository.delete(supplier);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.DELETE, "Fournisseur", supplier.getName(), SecurityUtils.currentUserId()));
  }

  private Supplier getOrThrow(UUID id) {
    return supplierRepository
        .findById(id)
        .orElseThrow(() -> ResourceNotFoundException.of("Fournisseur", id));
  }
}
