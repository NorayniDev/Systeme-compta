package com.facturationpme;

import static org.assertj.core.api.Assertions.assertThat;

import com.facturationpme.auth.dto.LoginRequest;
import com.facturationpme.auth.dto.LoginResponse;
import com.facturationpme.clients.dto.ClientCreateDto;
import com.facturationpme.clients.dto.ClientResponse;
import com.facturationpme.common.dto.ApiErrorResponse;
import com.facturationpme.common.dto.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Parcours bout-en-bout : connexion avec le compte demo seede par Flyway, puis utilisation du token
 * pour lister et creer des clients. Verifie le contrat JSON reellement expose, pas seulement le
 * comportement unitaire des services.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthAndClientsIT {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void configureDatasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  private String baseUrl() {
    return "http://localhost:" + port + "/api/v1";
  }

  @Test
  void loginThenListAndCreateClientShouldSucceed() {
    LoginRequest loginRequest = new LoginRequest("admin@facturation-pme.sn", "Admin123!", false);
    ResponseEntity<LoginResponse> loginResponse =
        restTemplate.postForEntity(baseUrl() + "/auth/login", loginRequest, LoginResponse.class);

    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    LoginResponse body = loginResponse.getBody();
    assertThat(body).isNotNull();
    assertThat(body.accessToken()).isNotBlank();
    assertThat(body.user().role().name()).isEqualTo("ADMIN");

    HttpHeaders authHeaders = new HttpHeaders();
    authHeaders.setBearerAuth(body.accessToken());

    ResponseEntity<PageResponse<ClientResponse>> listResponse =
        restTemplate.exchange(
            baseUrl() + "/clients",
            org.springframework.http.HttpMethod.GET,
            new HttpEntity<>(authHeaders),
            new org.springframework.core.ParameterizedTypeReference<>() {});

    assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listResponse.getBody()).isNotNull();
    assertThat(listResponse.getBody().totalElements()).isGreaterThanOrEqualTo(3);

    ClientCreateDto createDto =
        new ClientCreateDto(
            "Teranga Digital", "contact@teranga.sn", "+221771234567", "Dakar", "NINEA9998877");
    ResponseEntity<ClientResponse> createResponse =
        restTemplate.postForEntity(
            baseUrl() + "/clients", new HttpEntity<>(createDto, authHeaders), ClientResponse.class);

    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(createResponse.getBody()).isNotNull();
    assertThat(createResponse.getBody().email()).isEqualTo("contact@teranga.sn");
    assertThat(createResponse.getBody().status().name()).isEqualTo("ACTIVE");
  }

  @Test
  void listClientsWithoutTokenShouldReturnUnauthorizedWithApiErrorShape() {
    ResponseEntity<ApiErrorResponse> response =
        restTemplate.getForEntity(baseUrl() + "/clients", ApiErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(401);
    assertThat(response.getBody().code()).isEqualTo("UNAUTHORIZED");
  }
}
