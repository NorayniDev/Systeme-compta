package com.facturationpme.products;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.facturationpme.common.exception.DuplicateResourceException;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.products.domain.Product;
import com.facturationpme.products.domain.ProductStatus;
import com.facturationpme.products.domain.ProductUnit;
import com.facturationpme.products.dto.ProductCreateDto;
import com.facturationpme.products.dto.ProductResponse;
import com.facturationpme.products.dto.ProductUpdateDto;
import com.facturationpme.products.mapper.ProductMapper;
import com.facturationpme.products.repository.ProductRepository;
import com.facturationpme.products.service.ProductService;
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
class ProductServiceTest {

  @Mock private ProductRepository productRepository;
  @Mock private ProductMapper productMapper;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private ProductService productService;

  private Product product;
  private UUID productId;

  @BeforeEach
  void setUp() {
    productId = UUID.randomUUID();
    product =
        Product.builder()
            .id(productId)
            .name("Ramette de papier A4")
            .sku("PROD-0001")
            .unitPrice(BigDecimal.valueOf(3500))
            .taxRate(BigDecimal.valueOf(18))
            .unit(ProductUnit.BOX)
            .stockQuantity(240)
            .status(ProductStatus.ACTIVE)
            .build();
  }

  private ProductResponse responseFor(Product source) {
    return new ProductResponse(
        source.getId().toString(),
        source.getName(),
        source.getSku(),
        source.getDescription(),
        source.getCategory(),
        source.getUnitPrice(),
        source.getTaxRate(),
        source.getUnit(),
        source.getStockQuantity(),
        source.getStatus(),
        source.getCreatedAt(),
        source.getUpdatedAt());
  }

  @Test
  void createShouldRejectDuplicateSku() {
    ProductCreateDto dto =
        new ProductCreateDto(
            "Ramette",
            "PROD-0001",
            null,
            null,
            BigDecimal.TEN,
            BigDecimal.ZERO,
            ProductUnit.BOX,
            10);
    when(productRepository.existsBySkuIgnoreCase("PROD-0001")).thenReturn(true);

    assertThatThrownBy(() -> productService.create(dto))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void createShouldPersistNewActiveProduct() {
    ProductCreateDto dto =
        new ProductCreateDto(
            "Ramette de papier A4",
            "PROD-0001",
            null,
            null,
            BigDecimal.valueOf(3500),
            BigDecimal.valueOf(18),
            ProductUnit.BOX,
            240);
    when(productRepository.existsBySkuIgnoreCase("PROD-0001")).thenReturn(false);
    when(productMapper.toEntity(dto)).thenReturn(product);
    when(productRepository.save(product)).thenReturn(product);
    when(productMapper.toResponse(product)).thenReturn(responseFor(product));

    ProductResponse response = productService.create(dto);

    assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
    assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
  }

  @Test
  void findByIdShouldThrowWhenMissing() {
    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.findById(productId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void findByIdShouldReturnResponseWhenFound() {
    ProductResponse expected = responseFor(product);
    when(productRepository.findById(productId)).thenReturn(Optional.of(product));
    when(productMapper.toResponse(product)).thenReturn(expected);

    ProductResponse response = productService.findById(productId);

    assertThat(response).isEqualTo(expected);
  }

  @Test
  void searchShouldMapRepositoryPageToResponsePage() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Product> page = new PageImpl<>(List.of(product));
    ProductResponse expected = responseFor(product);
    when(productRepository.findAll(ArgumentMatchers.<Specification<Product>>any(), eq(pageable)))
        .thenReturn(page);
    when(productMapper.toResponse(product)).thenReturn(expected);

    Page<ProductResponse> result = productService.search("papier", pageable);

    assertThat(result.getContent()).containsExactly(expected);
  }

  @Test
  void updateShouldPersistChangesWhenValid() {
    ProductUpdateDto dto =
        new ProductUpdateDto(
            "Ramette de papier A4 - promo",
            "PROD-0001",
            null,
            null,
            BigDecimal.valueOf(2999),
            BigDecimal.valueOf(18),
            ProductUnit.BOX,
            200,
            ProductStatus.ACTIVE);
    ProductResponse expected = responseFor(product);
    when(productRepository.findById(productId)).thenReturn(Optional.of(product));
    when(productRepository.save(product)).thenReturn(product);
    when(productMapper.toResponse(product)).thenReturn(expected);

    ProductResponse response = productService.update(productId, dto);

    assertThat(response).isEqualTo(expected);
    verify(productMapper).updateEntityFromDto(dto, product);
  }

  @Test
  void updateShouldRejectSkuAlreadyUsedByAnotherProduct() {
    ProductUpdateDto dto =
        new ProductUpdateDto(
            "Ramette",
            "PROD-9999",
            null,
            null,
            BigDecimal.TEN,
            BigDecimal.ZERO,
            ProductUnit.BOX,
            10,
            ProductStatus.ACTIVE);
    when(productRepository.findById(productId)).thenReturn(Optional.of(product));
    when(productRepository.existsBySkuIgnoreCase("PROD-9999")).thenReturn(true);

    assertThatThrownBy(() -> productService.update(productId, dto))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void deleteShouldRemoveExistingProduct() {
    when(productRepository.findById(productId)).thenReturn(Optional.of(product));

    productService.delete(productId);

    verify(productRepository).delete(product);
  }

  @Test
  void deleteShouldThrowWhenProductMissing() {
    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.delete(productId))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
