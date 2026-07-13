package com.facturationpme.suppliers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.facturationpme.common.exception.DuplicateResourceException;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.suppliers.domain.Supplier;
import com.facturationpme.suppliers.domain.SupplierStatus;
import com.facturationpme.suppliers.dto.SupplierCreateDto;
import com.facturationpme.suppliers.dto.SupplierResponse;
import com.facturationpme.suppliers.dto.SupplierUpdateDto;
import com.facturationpme.suppliers.mapper.SupplierMapper;
import com.facturationpme.suppliers.repository.SupplierRepository;
import com.facturationpme.suppliers.service.SupplierService;
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
class SupplierServiceTest {

  @Mock private SupplierRepository supplierRepository;
  @Mock private SupplierMapper supplierMapper;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private SupplierService supplierService;

  private Supplier supplier;
  private UUID supplierId;

  @BeforeEach
  void setUp() {
    supplierId = UUID.randomUUID();
    supplier =
        Supplier.builder()
            .id(supplierId)
            .name("Sahel Fournitures SARL")
            .email("contact@sahel-fournitures.sn")
            .status(SupplierStatus.ACTIVE)
            .totalPurchased(BigDecimal.ZERO)
            .build();
  }

  @Test
  void createShouldRejectDuplicateEmail() {
    SupplierCreateDto dto =
        new SupplierCreateDto("Sahel", "contact@sahel-fournitures.sn", null, null, null);
    when(supplierRepository.existsByEmailIgnoreCase("contact@sahel-fournitures.sn"))
        .thenReturn(true);

    assertThatThrownBy(() -> supplierService.create(dto))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void createShouldPersistNewActiveSupplierWithZeroPurchasedAmount() {
    SupplierCreateDto dto =
        new SupplierCreateDto("Sahel", "contact@sahel-fournitures.sn", null, null, null);
    when(supplierRepository.existsByEmailIgnoreCase("contact@sahel-fournitures.sn"))
        .thenReturn(false);
    when(supplierMapper.toEntity(dto)).thenReturn(supplier);
    when(supplierRepository.save(supplier)).thenReturn(supplier);
    when(supplierMapper.toResponse(supplier))
        .thenReturn(
            new SupplierResponse(
                supplierId.toString(),
                "Sahel",
                "contact@sahel-fournitures.sn",
                null,
                null,
                null,
                SupplierStatus.ACTIVE,
                BigDecimal.ZERO,
                null,
                null));

    SupplierResponse response = supplierService.create(dto);

    assertThat(response.status()).isEqualTo(SupplierStatus.ACTIVE);
    assertThat(supplier.getStatus()).isEqualTo(SupplierStatus.ACTIVE);
    assertThat(supplier.getTotalPurchased()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void findByIdShouldThrowWhenMissing() {
    when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> supplierService.findById(supplierId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void findByIdShouldReturnResponseWhenFound() {
    SupplierResponse expected =
        new SupplierResponse(
            supplierId.toString(),
            "Sahel Fournitures SARL",
            "contact@sahel-fournitures.sn",
            null,
            null,
            null,
            SupplierStatus.ACTIVE,
            BigDecimal.ZERO,
            null,
            null);
    when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
    when(supplierMapper.toResponse(supplier)).thenReturn(expected);

    SupplierResponse response = supplierService.findById(supplierId);

    assertThat(response).isEqualTo(expected);
  }

  @Test
  void searchShouldMapRepositoryPageToResponsePage() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Supplier> page = new PageImpl<>(List.of(supplier));
    SupplierResponse expected =
        new SupplierResponse(
            supplierId.toString(),
            "Sahel Fournitures SARL",
            "contact@sahel-fournitures.sn",
            null,
            null,
            null,
            SupplierStatus.ACTIVE,
            BigDecimal.ZERO,
            null,
            null);
    when(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
        .thenReturn(page);
    when(supplierMapper.toResponse(supplier)).thenReturn(expected);

    Page<SupplierResponse> result = supplierService.search("sahel", pageable);

    assertThat(result.getContent()).containsExactly(expected);
  }

  @Test
  void updateShouldPersistChangesWhenValid() {
    SupplierUpdateDto dto =
        new SupplierUpdateDto(
            "Sahel Updated",
            "contact@sahel-fournitures.sn",
            null,
            null,
            null,
            SupplierStatus.INACTIVE);
    SupplierResponse expected =
        new SupplierResponse(
            supplierId.toString(),
            "Sahel Updated",
            "contact@sahel-fournitures.sn",
            null,
            null,
            null,
            SupplierStatus.INACTIVE,
            BigDecimal.ZERO,
            null,
            null);
    when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
    when(supplierRepository.save(supplier)).thenReturn(supplier);
    when(supplierMapper.toResponse(supplier)).thenReturn(expected);

    SupplierResponse response = supplierService.update(supplierId, dto);

    assertThat(response).isEqualTo(expected);
    verify(supplierMapper).updateEntityFromDto(dto, supplier);
  }

  @Test
  void updateShouldRejectEmailAlreadyUsedByAnotherSupplier() {
    SupplierUpdateDto dto =
        new SupplierUpdateDto("Sahel", "other@sahel.sn", null, null, null, SupplierStatus.ACTIVE);
    when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
    when(supplierRepository.existsByEmailIgnoreCase("other@sahel.sn")).thenReturn(true);

    assertThatThrownBy(() -> supplierService.update(supplierId, dto))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void deleteShouldRemoveExistingSupplier() {
    when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));

    supplierService.delete(supplierId);

    verify(supplierRepository).delete(supplier);
  }

  @Test
  void deleteShouldThrowWhenSupplierMissing() {
    when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> supplierService.delete(supplierId))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
