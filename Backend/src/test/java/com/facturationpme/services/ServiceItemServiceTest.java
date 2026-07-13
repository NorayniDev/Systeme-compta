package com.facturationpme.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.facturationpme.common.exception.DuplicateResourceException;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.services.domain.ServiceItem;
import com.facturationpme.services.domain.ServiceItemStatus;
import com.facturationpme.services.domain.ServiceItemUnit;
import com.facturationpme.services.dto.ServiceItemCreateDto;
import com.facturationpme.services.dto.ServiceItemResponse;
import com.facturationpme.services.dto.ServiceItemUpdateDto;
import com.facturationpme.services.mapper.ServiceItemMapper;
import com.facturationpme.services.repository.ServiceItemRepository;
import com.facturationpme.services.service.ServiceItemService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ServiceItemServiceTest {

  @Mock private ServiceItemRepository serviceItemRepository;
  @Mock private ServiceItemMapper serviceItemMapper;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private ServiceItemService serviceItemService;

  private ServiceItem serviceItem;
  private UUID serviceItemId;

  @BeforeEach
  void setUp() {
    serviceItemId = UUID.randomUUID();
    serviceItem =
        ServiceItem.builder()
            .id(serviceItemId)
            .name("Consultation comptable")
            .code("SERV-0001")
            .unitPrice(BigDecimal.valueOf(25000))
            .taxRate(BigDecimal.valueOf(18))
            .unit(ServiceItemUnit.HOUR)
            .status(ServiceItemStatus.ACTIVE)
            .build();
  }

  private ServiceItemResponse responseFor(ServiceItem source) {
    return new ServiceItemResponse(
        source.getId().toString(),
        source.getName(),
        source.getCode(),
        source.getDescription(),
        source.getCategory(),
        source.getUnitPrice(),
        source.getTaxRate(),
        source.getUnit(),
        source.getStatus(),
        source.getCreatedAt(),
        source.getUpdatedAt());
  }

  @Test
  void createShouldRejectDuplicateCode() {
    ServiceItemCreateDto dto =
        new ServiceItemCreateDto(
            "Consultation",
            "SERV-0001",
            null,
            null,
            BigDecimal.TEN,
            BigDecimal.ZERO,
            ServiceItemUnit.HOUR);
    when(serviceItemRepository.existsByCodeIgnoreCase("SERV-0001")).thenReturn(true);

    assertThatThrownBy(() -> serviceItemService.create(dto))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void createShouldPersistNewActiveServiceItem() {
    ServiceItemCreateDto dto =
        new ServiceItemCreateDto(
            "Consultation comptable",
            "SERV-0001",
            null,
            null,
            BigDecimal.valueOf(25000),
            BigDecimal.valueOf(18),
            ServiceItemUnit.HOUR);
    when(serviceItemRepository.existsByCodeIgnoreCase("SERV-0001")).thenReturn(false);
    when(serviceItemMapper.toEntity(dto)).thenReturn(serviceItem);
    when(serviceItemRepository.save(serviceItem)).thenReturn(serviceItem);
    when(serviceItemMapper.toResponse(serviceItem)).thenReturn(responseFor(serviceItem));

    ServiceItemResponse response = serviceItemService.create(dto);

    assertThat(response.status()).isEqualTo(ServiceItemStatus.ACTIVE);
    assertThat(serviceItem.getStatus()).isEqualTo(ServiceItemStatus.ACTIVE);
  }

  @Test
  void findByIdShouldThrowWhenMissing() {
    when(serviceItemRepository.findById(serviceItemId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> serviceItemService.findById(serviceItemId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void findByIdShouldReturnResponseWhenFound() {
    ServiceItemResponse expected = responseFor(serviceItem);
    when(serviceItemRepository.findById(serviceItemId)).thenReturn(Optional.of(serviceItem));
    when(serviceItemMapper.toResponse(serviceItem)).thenReturn(expected);

    ServiceItemResponse response = serviceItemService.findById(serviceItemId);

    assertThat(response).isEqualTo(expected);
  }

  @Test
  void searchShouldMapRepositoryPageToResponsePage() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<ServiceItem> page = new PageImpl<>(List.of(serviceItem));
    ServiceItemResponse expected = responseFor(serviceItem);
    when(serviceItemRepository.findAll(
            ArgumentMatchers.<Specification<ServiceItem>>any(), eq(pageable)))
        .thenReturn(page);
    when(serviceItemMapper.toResponse(serviceItem)).thenReturn(expected);

    Page<ServiceItemResponse> result = serviceItemService.search("consultation", pageable);

    assertThat(result.getContent()).containsExactly(expected);
  }

  @Test
  void updateShouldPersistChangesWhenValid() {
    ServiceItemUpdateDto dto =
        new ServiceItemUpdateDto(
            "Consultation comptable premium",
            "SERV-0001",
            null,
            null,
            BigDecimal.valueOf(30000),
            BigDecimal.valueOf(18),
            ServiceItemUnit.HOUR,
            ServiceItemStatus.ACTIVE);
    ServiceItemResponse expected = responseFor(serviceItem);
    when(serviceItemRepository.findById(serviceItemId)).thenReturn(Optional.of(serviceItem));
    when(serviceItemRepository.save(serviceItem)).thenReturn(serviceItem);
    when(serviceItemMapper.toResponse(serviceItem)).thenReturn(expected);

    ServiceItemResponse response = serviceItemService.update(serviceItemId, dto);

    assertThat(response).isEqualTo(expected);
    verify(serviceItemMapper).updateEntityFromDto(dto, serviceItem);
  }

  @Test
  void updateShouldRejectCodeAlreadyUsedByAnotherServiceItem() {
    ServiceItemUpdateDto dto =
        new ServiceItemUpdateDto(
            "Consultation",
            "SERV-9999",
            null,
            null,
            BigDecimal.TEN,
            BigDecimal.ZERO,
            ServiceItemUnit.HOUR,
            ServiceItemStatus.ACTIVE);
    when(serviceItemRepository.findById(serviceItemId)).thenReturn(Optional.of(serviceItem));
    when(serviceItemRepository.existsByCodeIgnoreCase("SERV-9999")).thenReturn(true);

    assertThatThrownBy(() -> serviceItemService.update(serviceItemId, dto))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void deleteShouldRemoveExistingServiceItem() {
    when(serviceItemRepository.findById(serviceItemId)).thenReturn(Optional.of(serviceItem));

    serviceItemService.delete(serviceItemId);

    verify(serviceItemRepository).delete(serviceItem);
  }

  @Test
  void deleteShouldThrowWhenServiceItemMissing() {
    when(serviceItemRepository.findById(serviceItemId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> serviceItemService.delete(serviceItemId))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
