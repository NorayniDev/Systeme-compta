package com.facturationpme.services.service;

import com.facturationpme.audit.domain.AuditAction;
import com.facturationpme.audit.event.AuditableActionEvent;
import com.facturationpme.common.exception.DuplicateResourceException;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.common.security.SecurityUtils;
import com.facturationpme.services.domain.ServiceItem;
import com.facturationpme.services.domain.ServiceItemStatus;
import com.facturationpme.services.dto.ServiceItemCreateDto;
import com.facturationpme.services.dto.ServiceItemResponse;
import com.facturationpme.services.dto.ServiceItemUpdateDto;
import com.facturationpme.services.mapper.ServiceItemMapper;
import com.facturationpme.services.repository.ServiceItemRepository;
import com.facturationpme.services.repository.ServiceItemSpecifications;
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
public class ServiceItemService {

  private final ServiceItemRepository serviceItemRepository;
  private final ServiceItemMapper serviceItemMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public Page<ServiceItemResponse> search(String query, Pageable pageable) {
    Specification<ServiceItem> specification =
        Specification.where(ServiceItemSpecifications.matchingQuery(query));
    return serviceItemRepository
        .findAll(specification, pageable)
        .map(serviceItemMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public ServiceItemResponse findById(UUID id) {
    return serviceItemMapper.toResponse(getOrThrow(id));
  }

  @Transactional
  public ServiceItemResponse create(ServiceItemCreateDto dto) {
    if (serviceItemRepository.existsByCodeIgnoreCase(dto.code())) {
      throw new DuplicateResourceException("Une prestation utilise deja le code : " + dto.code());
    }
    ServiceItem serviceItem = serviceItemMapper.toEntity(dto);
    serviceItem.setStatus(ServiceItemStatus.ACTIVE);
    ServiceItem saved = serviceItemRepository.save(serviceItem);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.CREATE, "Service", saved.getName(), SecurityUtils.currentUserId()));
    return serviceItemMapper.toResponse(saved);
  }

  @Transactional
  public ServiceItemResponse update(UUID id, ServiceItemUpdateDto dto) {
    ServiceItem serviceItem = getOrThrow(id);
    if (!serviceItem.getCode().equalsIgnoreCase(dto.code())
        && serviceItemRepository.existsByCodeIgnoreCase(dto.code())) {
      throw new DuplicateResourceException("Une prestation utilise deja le code : " + dto.code());
    }
    serviceItemMapper.updateEntityFromDto(dto, serviceItem);
    ServiceItem saved = serviceItemRepository.save(serviceItem);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.UPDATE, "Service", saved.getName(), SecurityUtils.currentUserId()));
    return serviceItemMapper.toResponse(saved);
  }

  @Transactional
  public void delete(UUID id) {
    ServiceItem serviceItem = getOrThrow(id);
    serviceItemRepository.delete(serviceItem);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.DELETE, "Service", serviceItem.getName(), SecurityUtils.currentUserId()));
  }

  private ServiceItem getOrThrow(UUID id) {
    return serviceItemRepository
        .findById(id)
        .orElseThrow(() -> ResourceNotFoundException.of("Prestation", id));
  }
}
