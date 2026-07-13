package com.facturationpme.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.facturationpme.auth.domain.RefreshToken;
import com.facturationpme.auth.repository.RefreshTokenRepository;
import com.facturationpme.common.config.JwtProperties;
import com.facturationpme.common.security.TokenHasher;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  @Mock private RefreshTokenRepository refreshTokenRepository;

  private RefreshTokenService refreshTokenService;
  private UUID userId;

  @BeforeEach
  void setUp() {
    JwtProperties jwtProperties = new JwtProperties("test-secret", 900_000L, 604_800_000L);
    refreshTokenService = new RefreshTokenService(refreshTokenRepository, jwtProperties);
    userId = UUID.randomUUID();
  }

  @Test
  void issueShouldPersistHashedTokenAndReturnRawToken() {
    ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
    when(refreshTokenRepository.save(captor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    String rawToken = refreshTokenService.issue(userId);

    RefreshToken persisted = captor.getValue();
    assertThat(persisted.getUserId()).isEqualTo(userId);
    assertThat(persisted.getTokenHash()).isEqualTo(TokenHasher.hash(rawToken));
    assertThat(persisted.getTokenHash()).isNotEqualTo(rawToken);
    assertThat(persisted.isRevoked()).isFalse();
  }

  @Test
  void rotateShouldRevokeTokenAndReturnUserIdWhenValid() {
    String rawToken = TokenHasher.generateOpaqueToken();
    RefreshToken stored =
        RefreshToken.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .tokenHash(TokenHasher.hash(rawToken))
            .expiresAt(Instant.now().plusSeconds(3600))
            .revoked(false)
            .createdAt(Instant.now())
            .build();
    when(refreshTokenRepository.findByTokenHash(TokenHasher.hash(rawToken)))
        .thenReturn(Optional.of(stored));

    UUID result = refreshTokenService.rotate(rawToken);

    assertThat(result).isEqualTo(userId);
    assertThat(stored.isRevoked()).isTrue();
    verify(refreshTokenRepository).save(stored);
  }

  @Test
  void rotateShouldRejectUnknownToken() {
    when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> refreshTokenService.rotate("unknown"))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void rotateShouldRejectExpiredToken() {
    String rawToken = TokenHasher.generateOpaqueToken();
    RefreshToken expired =
        RefreshToken.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .tokenHash(TokenHasher.hash(rawToken))
            .expiresAt(Instant.now().minusSeconds(1))
            .revoked(false)
            .createdAt(Instant.now())
            .build();
    when(refreshTokenRepository.findByTokenHash(TokenHasher.hash(rawToken)))
        .thenReturn(Optional.of(expired));

    assertThatThrownBy(() -> refreshTokenService.rotate(rawToken))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void rotateShouldRejectAlreadyRevokedToken() {
    String rawToken = TokenHasher.generateOpaqueToken();
    RefreshToken revoked =
        RefreshToken.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .tokenHash(TokenHasher.hash(rawToken))
            .expiresAt(Instant.now().plusSeconds(3600))
            .revoked(true)
            .createdAt(Instant.now())
            .build();
    when(refreshTokenRepository.findByTokenHash(TokenHasher.hash(rawToken)))
        .thenReturn(Optional.of(revoked));

    assertThatThrownBy(() -> refreshTokenService.rotate(rawToken))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void revokeAllForUserShouldDelegateToRepository() {
    refreshTokenService.revokeAllForUser(userId);

    verify(refreshTokenRepository).revokeAllForUser(userId);
  }
}
