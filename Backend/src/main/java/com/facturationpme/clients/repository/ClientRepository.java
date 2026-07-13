package com.facturationpme.clients.repository;

import com.facturationpme.clients.domain.Client;
import com.facturationpme.clients.domain.ClientStatus;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClientRepository
    extends JpaRepository<Client, UUID>, JpaSpecificationExecutor<Client> {

  boolean existsByEmailIgnoreCase(String email);

  long countByStatus(ClientStatus status);
}
