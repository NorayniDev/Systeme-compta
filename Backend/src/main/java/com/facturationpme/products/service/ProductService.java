package com.facturationpme.products.service;

import com.facturationpme.audit.domain.AuditAction;
import com.facturationpme.audit.event.AuditableActionEvent;
import com.facturationpme.common.exception.DuplicateResourceException;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.common.security.SecurityUtils;
import com.facturationpme.products.domain.Product;
import com.facturationpme.products.domain.ProductStatus;
import com.facturationpme.products.dto.ProductCreateDto;
import com.facturationpme.products.dto.ProductResponse;
import com.facturationpme.products.dto.ProductUpdateDto;
import com.facturationpme.products.mapper.ProductMapper;
import com.facturationpme.products.repository.ProductRepository;
import com.facturationpme.products.repository.ProductSpecifications;
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
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public Page<ProductResponse> search(String query, Pageable pageable) {
    Specification<Product> specification =
        Specification.where(ProductSpecifications.matchingQuery(query));
    return productRepository.findAll(specification, pageable).map(productMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public ProductResponse findById(UUID id) {
    return productMapper.toResponse(getOrThrow(id));
  }

  @Transactional
  public ProductResponse create(ProductCreateDto dto) {
    if (productRepository.existsBySkuIgnoreCase(dto.sku())) {
      throw new DuplicateResourceException("Un produit utilise deja le SKU : " + dto.sku());
    }
    Product product = productMapper.toEntity(dto);
    product.setStatus(ProductStatus.ACTIVE);
    Product saved = productRepository.save(product);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.CREATE, "Produit", saved.getName(), SecurityUtils.currentUserId()));
    return productMapper.toResponse(saved);
  }

  @Transactional
  public ProductResponse update(UUID id, ProductUpdateDto dto) {
    Product product = getOrThrow(id);
    if (!product.getSku().equalsIgnoreCase(dto.sku())
        && productRepository.existsBySkuIgnoreCase(dto.sku())) {
      throw new DuplicateResourceException("Un produit utilise deja le SKU : " + dto.sku());
    }
    productMapper.updateEntityFromDto(dto, product);
    Product saved = productRepository.save(product);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.UPDATE, "Produit", saved.getName(), SecurityUtils.currentUserId()));
    return productMapper.toResponse(saved);
  }

  @Transactional
  public void delete(UUID id) {
    Product product = getOrThrow(id);
    productRepository.delete(product);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.DELETE, "Produit", product.getName(), SecurityUtils.currentUserId()));
  }

  private Product getOrThrow(UUID id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> ResourceNotFoundException.of("Produit", id));
  }
}
