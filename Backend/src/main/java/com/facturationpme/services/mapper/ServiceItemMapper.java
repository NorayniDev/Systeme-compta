package com.facturationpme.services.mapper;

import com.facturationpme.services.domain.ServiceItem;
import com.facturationpme.services.dto.ServiceItemCreateDto;
import com.facturationpme.services.dto.ServiceItemResponse;
import com.facturationpme.services.dto.ServiceItemUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ServiceItemMapper {

  ServiceItemResponse toResponse(ServiceItem serviceItem);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  void updateEntityFromDto(ServiceItemUpdateDto dto, @MappingTarget ServiceItem serviceItem);

  default ServiceItem toEntity(ServiceItemCreateDto dto) {
    return ServiceItem.builder()
        .name(dto.name())
        .code(dto.code())
        .description(dto.description())
        .category(dto.category())
        .unitPrice(dto.unitPrice())
        .taxRate(dto.taxRate())
        .unit(dto.unit())
        .build();
  }
}
