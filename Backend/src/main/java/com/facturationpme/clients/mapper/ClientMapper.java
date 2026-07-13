package com.facturationpme.clients.mapper;

import com.facturationpme.clients.domain.Client;
import com.facturationpme.clients.dto.ClientCreateDto;
import com.facturationpme.clients.dto.ClientResponse;
import com.facturationpme.clients.dto.ClientUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ClientMapper {

  ClientResponse toResponse(Client client);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "totalInvoiced", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  void updateEntityFromDto(ClientUpdateDto dto, @MappingTarget Client client);

  default Client toEntity(ClientCreateDto dto) {
    return Client.builder()
        .name(dto.name())
        .email(dto.email())
        .phone(dto.phone())
        .address(dto.address())
        .taxId(dto.taxId())
        .build();
  }
}
