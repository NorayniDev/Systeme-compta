package com.facturationpme.clients.service;

import com.facturationpme.audit.domain.AuditAction;
import com.facturationpme.audit.event.AuditableActionEvent;
import com.facturationpme.clients.domain.Client;
import com.facturationpme.clients.domain.ClientStatus;
import com.facturationpme.clients.dto.ClientCreateDto;
import com.facturationpme.clients.dto.ClientResponse;
import com.facturationpme.clients.dto.ClientUpdateDto;
import com.facturationpme.clients.mapper.ClientMapper;
import com.facturationpme.clients.repository.ClientRepository;
import com.facturationpme.clients.repository.ClientSpecifications;
import com.facturationpme.common.exception.DuplicateResourceException;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.common.security.SecurityUtils;
import java.math.BigDecimal;
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
public class ClientService {

  private final ClientRepository clientRepository;
  private final ClientMapper clientMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public Page<ClientResponse> search(String query, Pageable pageable) {
    Specification<Client> specification =
        Specification.where(ClientSpecifications.matchingQuery(query));
    return clientRepository.findAll(specification, pageable).map(clientMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public ClientResponse findById(UUID id) {
    return clientMapper.toResponse(getOrThrow(id));
  }

  @Transactional
  public ClientResponse create(ClientCreateDto dto) {
    if (clientRepository.existsByEmailIgnoreCase(dto.email())) {
      throw new DuplicateResourceException(
          "Un client utilise deja l'adresse email : " + dto.email());
    }
    Client client = clientMapper.toEntity(dto);
    client.setStatus(ClientStatus.ACTIVE);
    client.setTotalInvoiced(BigDecimal.ZERO);
    Client saved = clientRepository.save(client);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.CREATE, "Client", saved.getName(), SecurityUtils.currentUserId()));
    return clientMapper.toResponse(saved);
  }

  @Transactional
  public ClientResponse update(UUID id, ClientUpdateDto dto) {
    Client client = getOrThrow(id);
    if (!client.getEmail().equalsIgnoreCase(dto.email())
        && clientRepository.existsByEmailIgnoreCase(dto.email())) {
      throw new DuplicateResourceException(
          "Un client utilise deja l'adresse email : " + dto.email());
    }
    clientMapper.updateEntityFromDto(dto, client);
    Client saved = clientRepository.save(client);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.UPDATE, "Client", saved.getName(), SecurityUtils.currentUserId()));
    return clientMapper.toResponse(saved);
  }

  @Transactional
  public void delete(UUID id) {
    Client client = getOrThrow(id);
    clientRepository.delete(client);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.DELETE, "Client", client.getName(), SecurityUtils.currentUserId()));
  }

  private Client getOrThrow(UUID id) {
    return clientRepository
        .findById(id)
        .orElseThrow(() -> ResourceNotFoundException.of("Client", id));
  }
}
