package com.facturationpme.suppliers.mapper;

import com.facturationpme.suppliers.domain.Supplier;
import com.facturationpme.suppliers.dto.SupplierCreateDto;
import com.facturationpme.suppliers.dto.SupplierResponse;
import com.facturationpme.suppliers.dto.SupplierUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

  SupplierResponse toResponse(Supplier supplier);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "totalPurchased", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  void updateEntityFromDto(SupplierUpdateDto dto, @MappingTarget Supplier supplier);

  default Supplier toEntity(SupplierCreateDto dto) {
    return Supplier.builder()
        .name(dto.name())
        .email(dto.email())
        .phone(dto.phone())
        .address(dto.address())
        .taxId(dto.taxId())
        .build();
  }
}
