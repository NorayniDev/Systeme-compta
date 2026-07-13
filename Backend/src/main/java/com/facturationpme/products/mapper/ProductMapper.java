package com.facturationpme.products.mapper;

import com.facturationpme.products.domain.Product;
import com.facturationpme.products.dto.ProductCreateDto;
import com.facturationpme.products.dto.ProductResponse;
import com.facturationpme.products.dto.ProductUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProductMapper {

  ProductResponse toResponse(Product product);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  void updateEntityFromDto(ProductUpdateDto dto, @MappingTarget Product product);

  default Product toEntity(ProductCreateDto dto) {
    return Product.builder()
        .name(dto.name())
        .sku(dto.sku())
        .description(dto.description())
        .category(dto.category())
        .unitPrice(dto.unitPrice())
        .taxRate(dto.taxRate())
        .unit(dto.unit())
        .stockQuantity(dto.stockQuantity())
        .build();
  }
}
