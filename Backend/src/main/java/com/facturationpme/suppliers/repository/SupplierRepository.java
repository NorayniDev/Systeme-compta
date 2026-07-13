package com.facturationpme.suppliers.repository;

import com.facturationpme.suppliers.domain.Supplier;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SupplierRepository
    extends JpaRepository<Supplier, UUID>, JpaSpecificationExecutor<Supplier> {

  boolean existsByEmailIgnoreCase(String email);
}
