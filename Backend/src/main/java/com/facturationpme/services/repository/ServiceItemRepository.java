package com.facturationpme.services.repository;

import com.facturationpme.services.domain.ServiceItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServiceItemRepository
    extends JpaRepository<ServiceItem, UUID>, JpaSpecificationExecutor<ServiceItem> {

  boolean existsByCodeIgnoreCase(String code);
}
