package com.facturationpme.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.facturationpme.clients.domain.Client;
import com.facturationpme.clients.domain.ClientStatus;
import com.facturationpme.clients.dto.ClientCreateDto;
import com.facturationpme.clients.dto.ClientResponse;
import com.facturationpme.clients.dto.ClientUpdateDto;
import com.facturationpme.clients.mapper.ClientMapper;
import com.facturationpme.clients.repository.ClientRepository;
import com.facturationpme.clients.service.ClientService;
import com.facturationpme.common.exception.DuplicateResourceException;
import com.facturationpme.common.exception.ResourceNotFoundException;
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
class ClientServiceTest {

  @Mock private ClientRepository clientRepository;
  @Mock private ClientMapper clientMapper;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private ClientService clientService;

  private Client client;
  private UUID clientId;

  @BeforeEach
  void setUp() {
    clientId = UUID.randomUUID();
    client =
        Client.builder()
            .id(clientId)
            .name("ACME Senegal SARL")
            .email("contact@acme.sn")
            .status(ClientStatus.ACTIVE)
            .totalInvoiced(BigDecimal.ZERO)
            .build();
  }

  @Test
  void createShouldRejectDuplicateEmail() {
    ClientCreateDto dto = new ClientCreateDto("ACME", "contact@acme.sn", null, null, null);
    when(clientRepository.existsByEmailIgnoreCase("contact@acme.sn")).thenReturn(true);

    assertThatThrownBy(() -> clientService.create(dto))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void createShouldPersistNewActiveClientWithZeroInvoicedAmount() {
    ClientCreateDto dto = new ClientCreateDto("ACME", "contact@acme.sn", null, null, null);
    when(clientRepository.existsByEmailIgnoreCase("contact@acme.sn")).thenReturn(false);
    when(clientMapper.toEntity(dto)).thenReturn(client);
    when(clientRepository.save(client)).thenReturn(client);
    when(clientMapper.toResponse(client))
        .thenReturn(
            new ClientResponse(
                clientId.toString(),
                "ACME",
                "contact@acme.sn",
                null,
                null,
                null,
                ClientStatus.ACTIVE,
                BigDecimal.ZERO,
                null,
                null));

    ClientResponse response = clientService.create(dto);

    assertThat(response.status()).isEqualTo(ClientStatus.ACTIVE);
    assertThat(client.getStatus()).isEqualTo(ClientStatus.ACTIVE);
    assertThat(client.getTotalInvoiced()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void findByIdShouldThrowWhenMissing() {
    when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> clientService.findById(clientId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void findByIdShouldReturnResponseWhenFound() {
    ClientResponse expected =
        new ClientResponse(
            clientId.toString(),
            "ACME",
            "contact@acme.sn",
            null,
            null,
            null,
            ClientStatus.ACTIVE,
            BigDecimal.ZERO,
            null,
            null);
    when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(clientMapper.toResponse(client)).thenReturn(expected);

    ClientResponse response = clientService.findById(clientId);

    assertThat(response).isEqualTo(expected);
  }

  @Test
  void searchShouldMapRepositoryPageToResponsePage() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Client> page = new PageImpl<>(List.of(client));
    ClientResponse expected =
        new ClientResponse(
            clientId.toString(),
            "ACME",
            "contact@acme.sn",
            null,
            null,
            null,
            ClientStatus.ACTIVE,
            BigDecimal.ZERO,
            null,
            null);
    when(clientRepository.findAll(ArgumentMatchers.<Specification<Client>>any(), eq(pageable)))
        .thenReturn(page);
    when(clientMapper.toResponse(client)).thenReturn(expected);

    Page<ClientResponse> result = clientService.search("acme", pageable);

    assertThat(result.getContent()).containsExactly(expected);
  }

  @Test
  void updateShouldPersistChangesWhenValid() {
    ClientUpdateDto dto =
        new ClientUpdateDto(
            "ACME Updated", "contact@acme.sn", null, null, null, ClientStatus.INACTIVE);
    ClientResponse expected =
        new ClientResponse(
            clientId.toString(),
            "ACME Updated",
            "contact@acme.sn",
            null,
            null,
            null,
            ClientStatus.INACTIVE,
            BigDecimal.ZERO,
            null,
            null);
    when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(clientRepository.save(client)).thenReturn(client);
    when(clientMapper.toResponse(client)).thenReturn(expected);

    ClientResponse response = clientService.update(clientId, dto);

    assertThat(response).isEqualTo(expected);
    verify(clientMapper).updateEntityFromDto(dto, client);
  }

  @Test
  void updateShouldRejectEmailAlreadyUsedByAnotherClient() {
    ClientUpdateDto dto =
        new ClientUpdateDto("ACME", "other@acme.sn", null, null, null, ClientStatus.ACTIVE);
    when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(clientRepository.existsByEmailIgnoreCase("other@acme.sn")).thenReturn(true);

    assertThatThrownBy(() -> clientService.update(clientId, dto))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void deleteShouldRemoveExistingClient() {
    when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

    clientService.delete(clientId);

    verify(clientRepository).delete(client);
  }

  @Test
  void deleteShouldThrowWhenClientMissing() {
    when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> clientService.delete(clientId))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
