package com.facturationpme.auth.service;

import com.facturationpme.auth.domain.PasswordResetToken;
import com.facturationpme.auth.repository.PasswordResetTokenRepository;
import com.facturationpme.common.exception.InvalidTokenException;
import com.facturationpme.common.security.TokenHasher;
import com.facturationpme.users.domain.User;
import com.facturationpme.users.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

  private static final Duration RESET_TOKEN_TTL = Duration.ofHours(1);

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordResetNotifier passwordResetNotifier;
  private final RefreshTokenService refreshTokenService;
  private final PasswordEncoder passwordEncoder;

  /**
   * Ne revele jamais si l'email existe ou non - toujours un succes silencieux du point de vue de
   * l'appelant.
   */
  @Transactional
  public void requestReset(String email) {
    userRepository
        .findByEmailIgnoreCase(email)
        .ifPresent(
            user -> {
              String rawToken = TokenHasher.generateOpaqueToken();
              PasswordResetToken token =
                  PasswordResetToken.builder()
                      .userId(user.getId())
                      .tokenHash(TokenHasher.hash(rawToken))
                      .expiresAt(Instant.now().plus(RESET_TOKEN_TTL))
                      .used(false)
                      .createdAt(Instant.now())
                      .build();
              passwordResetTokenRepository.save(token);
              passwordResetNotifier.notifyResetToken(user, rawToken);
            });
  }

  @Transactional
  public void reset(String rawToken, String newPassword) {
    PasswordResetToken token =
        passwordResetTokenRepository
            .findByTokenHash(TokenHasher.hash(rawToken))
            .orElseThrow(() -> new InvalidTokenException("Jeton de reinitialisation invalide."));

    if (token.isUsed() || token.getExpiresAt().isBefore(Instant.now())) {
      throw new InvalidTokenException("Jeton de reinitialisation expire ou deja utilise.");
    }

    User user =
        userRepository
            .findById(token.getUserId())
            .orElseThrow(() -> new InvalidTokenException("Utilisateur associe introuvable."));

    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    token.setUsed(true);
    passwordResetTokenRepository.save(token);

    refreshTokenService.revokeAllForUser(user.getId());
  }
}
