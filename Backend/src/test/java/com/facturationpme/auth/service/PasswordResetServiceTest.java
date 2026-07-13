package com.facturationpme.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.facturationpme.auth.domain.PasswordResetToken;
import com.facturationpme.auth.repository.PasswordResetTokenRepository;
import com.facturationpme.common.exception.InvalidTokenException;
import com.facturationpme.common.security.TokenHasher;
import com.facturationpme.users.domain.User;
import com.facturationpme.users.domain.UserRole;
import com.facturationpme.users.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
  @Mock private PasswordResetNotifier passwordResetNotifier;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private PasswordEncoder passwordEncoder;

  private PasswordResetService passwordResetService;

  @BeforeEach
  void setUp() {
    passwordResetService =
        new PasswordResetService(
            userRepository,
            passwordResetTokenRepository,
            passwordResetNotifier,
            refreshTokenService,
            passwordEncoder);
  }

  @Test
  void requestResetShouldDoNothingWhenEmailUnknown() {
    when(userRepository.findByEmailIgnoreCase("nobody@nowhere.sn")).thenReturn(Optional.empty());

    passwordResetService.requestReset("nobody@nowhere.sn");

    verify(passwordResetTokenRepository, never()).save(any());
    verify(passwordResetNotifier, never()).notifyResetToken(any(), any());
  }

  @Test
  void requestResetShouldPersistTokenAndNotifyWhenEmailKnown() {
    User user = User.builder().id(UUID.randomUUID()).email("admin@facturation-pme.sn").build();
    when(userRepository.findByEmailIgnoreCase("admin@facturation-pme.sn"))
        .thenReturn(Optional.of(user));

    passwordResetService.requestReset("admin@facturation-pme.sn");

    ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
    verify(passwordResetTokenRepository).save(captor.capture());
    assertThat(captor.getValue().getUserId()).isEqualTo(user.getId());
    assertThat(captor.getValue().isUsed()).isFalse();
    verify(passwordResetNotifier).notifyResetToken(eq(user), any());
  }

  @Test
  void resetShouldRejectUnknownToken() {
    when(passwordResetTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> passwordResetService.reset("bad-token", "newPassword123"))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  void resetShouldRejectExpiredToken() {
    String rawToken = TokenHasher.generateOpaqueToken();
    PasswordResetToken expired =
        PasswordResetToken.builder()
            .id(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .tokenHash(TokenHasher.hash(rawToken))
            .expiresAt(Instant.now().minusSeconds(1))
            .used(false)
            .createdAt(Instant.now())
            .build();
    when(passwordResetTokenRepository.findByTokenHash(TokenHasher.hash(rawToken)))
        .thenReturn(Optional.of(expired));

    assertThatThrownBy(() -> passwordResetService.reset(rawToken, "newPassword123"))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  void resetShouldRejectAlreadyUsedToken() {
    String rawToken = TokenHasher.generateOpaqueToken();
    PasswordResetToken used =
        PasswordResetToken.builder()
            .id(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .tokenHash(TokenHasher.hash(rawToken))
            .expiresAt(Instant.now().plusSeconds(3600))
            .used(true)
            .createdAt(Instant.now())
            .build();
    when(passwordResetTokenRepository.findByTokenHash(TokenHasher.hash(rawToken)))
        .thenReturn(Optional.of(used));

    assertThatThrownBy(() -> passwordResetService.reset(rawToken, "newPassword123"))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  void resetShouldUpdatePasswordMarkTokenUsedAndRevokeRefreshTokens() {
    UUID userId = UUID.randomUUID();
    String rawToken = TokenHasher.generateOpaqueToken();
    PasswordResetToken token =
        PasswordResetToken.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .tokenHash(TokenHasher.hash(rawToken))
            .expiresAt(Instant.now().plusSeconds(3600))
            .used(false)
            .createdAt(Instant.now())
            .build();
    User user = User.builder().id(userId).role(UserRole.ADMIN).build();
    when(passwordResetTokenRepository.findByTokenHash(TokenHasher.hash(rawToken)))
        .thenReturn(Optional.of(token));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode("newPassword123")).thenReturn("hashed-password");

    passwordResetService.reset(rawToken, "newPassword123");

    assertThat(user.getPasswordHash()).isEqualTo("hashed-password");
    assertThat(token.isUsed()).isTrue();
    verify(userRepository).save(user);
    verify(passwordResetTokenRepository).save(token);
    verify(refreshTokenService).revokeAllForUser(userId);
  }
}
